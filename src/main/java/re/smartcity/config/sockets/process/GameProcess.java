package re.smartcity.config.sockets.process;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import re.smartcity.common.data.Forecast;
import re.smartcity.common.utils.Interpolation;
import re.smartcity.config.sockets.GameSocketHandler;
import re.smartcity.energynet.GenerationUsageModes;
import re.smartcity.energynet.IComponentIdentification;
import re.smartcity.energynet.SupportedTypes;
import re.smartcity.energynet.component.*;
import re.smartcity.energynet.component.data.ConsumerSpecification;
import re.smartcity.energynet.component.data.EnergyStorageSpecification;
import re.smartcity.energynet.component.data.GenerationSpecification;
import re.smartcity.energynet.component.data.GreenGenerationSpecification;
import re.smartcity.modeling.ModelingData;
import re.smartcity.modeling.TaskData;
import re.smartcity.modeling.scheme.IConnectionPort;
import re.smartcity.modeling.scheme.IOesHub;
import re.smartcity.stand.SerialCommand;
import re.smartcity.stand.SerialElementAddresses;
import re.smartcity.stand.SerialPackageTypes;
import re.smartcity.stand.StandService;
import re.smartcity.wind.WindRouterHandlers;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

public class GameProcess implements Runnable {

    public final static int SECONDS_OF_DAY = 86400; // количество секунд в сутках
    public final static int SECONDS_OF_HOURS = 3600; // количество секунд в часе
    public final static int MODEL_DISCRET = 250; // время дискретизации в мс

    private final Logger logger = LoggerFactory.getLogger(GameProcess.class);

    private final TaskData task;
    private final GameSocketHandler messenger;
    private final ModelingData modelingData;
    private final StandService standService;
    private final WindRouterHandlers wind;

    public GameProcess(
            TaskData task,
            GameSocketHandler messenger,
            ModelingData modelingData,
            StandService standService,
            WindRouterHandlers wind
    ) {
        this.wind = wind;
        this.standService = standService;
        this.task = task;
        this.messenger = messenger;
        this.modelingData = modelingData;
    }

    private double getSecondInMillis() {
        return (((double) modelingData.getGamingDay().toSecondOfDay()) / ((double) SECONDS_OF_DAY)) * 1000;
    }

    private Map<Integer, PortTracertInternalData> buildPorts() {
        Map<Integer, PortTracertInternalData> ports = new HashMap<>();

        // обход входов и выходов основной подстанции
        Stream.concat(
                    Stream.of(this.task.getGameBlock().getRoot().getInputs()),
                    Stream.of(this.task.getGameBlock().getRoot().getOutputs())
                )
                .forEach(line -> {
                    ports.put(line.getAddress(), new PortTracertInternalData(line));
                    if (line.getConnections() != null) {
                        Arrays.stream(line.getConnections())
                                .forEach(e -> ports.put(e.getAddress(), new PortTracertInternalData(e, line.getAddress())));
                    }
                });

        // обработка миниподстанций: считаю, что только миниподстанции поддерживают выходные порты
        Arrays.stream(task.getGameBlock().getRoot().getDevices())
                .filter(IOesHub::supportOutputs)
                .flatMap(e -> Stream.of(e.getOutputs()))
                .forEach(line -> {
                    ports.put(line.getAddress(), new PortTracertInternalData(line));
                    if (line.getConnections() != null) {
                        Arrays.stream(line.getConnections())
                                .forEach(e -> ports.put(e.getAddress(), new PortTracertInternalData(e, line.getAddress())));
                    }
                });

        return ports;
    }

    private double[] getForecast(IComponentIdentification cmp, long count_pt, long game_step) {
        if (cmp.getComponentType() == SupportedTypes.CONSUMER || cmp.getComponentType() == SupportedTypes.GENERATOR) {
            Forecast forecast = null;
            double energy = 1.0;
            if (cmp.getComponentType() == SupportedTypes.CONSUMER) {
                ConsumerSpecification data = ((Consumer) cmp).getData();
                if (data.isUseforecast()) {
                    forecast = data.getForecast();
                    energy = data.getEnergy();
                }
            } else {
                GenerationSpecification data = ((Generation) cmp).getData();
                if (data.isUseforecast()) {
                    forecast = data.getForecast();
                    energy = data.getEnergy();
                }
            }
            if (forecast != null) {
                return Interpolation.interpolate(forecast.getData(), energy, count_pt, game_step);
            }
        }
        return null;
    }

    private Map<Integer, HubTracertInternalData> buildHubs(long cpt, long game_step) {
        Map<Integer, HubTracertInternalData> hubs = new HashMap<>();

        Arrays.stream(task.getGameBlock().getRoot().getDevices())
                .forEach(e -> {
                    IComponentIdentification cmp = Arrays.stream(modelingData.getAllobjects())
                            .filter(oes -> oes.getDevaddr() == e.getAddress())
                            .findFirst()
                            .orElseThrow();
                    HubTracertInternalData hubData = new HubTracertInternalData(e, cmp);
                    hubData.setForecast(getForecast(cmp, cpt, game_step));
                    hubs.put(e.getAddress(), hubData);
                });

        return hubs;
    }

    private Map<Integer, ElectricalSubnet> buildLines() {
        Map<Integer, ElectricalSubnet> lines = new HashMap<>();

        Stream.concat(
                        Stream.of(this.task.getPowerSystem().getData().getInputs()),
                        Stream.of(this.task.getPowerSystem().getData().getOutputs())
                )
                .forEach(line -> lines.put((int) line.getDevaddr(), line));

        // обработка миниподстанций: считаю, что только миниподстанции поддерживают выходные порты
        Arrays.stream(task.getGameBlock().getRoot().getDevices())
                .filter(IOesHub::supportOutputs)
                .map(IOesHub::getOwner)
                .filter(e -> e.getComponentType() == SupportedTypes.DISTRIBUTOR)
                .flatMap(e -> Stream.of(((EnergyDistributor) e).getData().getOutputs()))
                .forEach(line -> lines.put((int) line.getDevaddr(), line));

        return lines;
    }

    private void standAllOff() {
        wind.windOff();
        standService.pushSerialCommand(
                new SerialCommand(SerialElementAddresses.SUN_SIMULATOR,
                        SerialPackageTypes.SET_BRIGHTNESS_SUN_SIMULATOR, 0));
        standService.pushSerialCommand(
                new SerialCommand(SerialPackageTypes.SET_HIGHLIGHT_LEVEL, 0));
    }

    private double getSubsystemEnergy(HubTracertInternalData sub_data,
                                      PortTracertInternalData sub_port,
                                      Map<Integer, PortTracertInternalData> ports,
                                      Map<Integer, HubTracertInternalData> hubs,
                                      int fNumber,
                                      double energy_for_step) {
        Arrays.stream(sub_data.getHub().getOutputs())
                .filter(e -> e.getConnections() != null)
                .filter(IConnectionPort::isOn)
                .forEach(line -> {
                    PortTracertInternalData line_port = ports.get(line.getAddress());

                    Arrays.stream(line.getConnections())
                            .filter(IConnectionPort::isOn)
                            .forEach(conn -> {
                                PortTracertInternalData consumer_port = ports.get(conn.getAddress());
                                HubTracertInternalData consumer_data = hubs.get(consumer_port.getPort().getOwner().getAddress());
                                double v;

                                if (consumer_data.getOes().getComponentType() == SupportedTypes.DISTRIBUTOR) {
                                    v = getSubsystemEnergy(consumer_data, consumer_port,
                                            ports, hubs, fNumber, energy_for_step);
                                } else {
                                    if (consumer_data.useForecast()) {
                                        v = consumer_data.getForecast()[fNumber];
                                    } else {
                                        v = ((Consumer) consumer_data.getOes()).getData().getEnergy();
                                    }
                                }

                                v *= energy_for_step;
                                consumer_port.getTracert().getValues().setEnergy(v);
                                consumer_data.getTracert().getValues().setEnergy(v);
                                line_port.getTracert().getValues().setEnergy(
                                        line_port.getTracert().getValues().getEnergy() +
                                                consumer_data.getTracert().getValues().getEnergy());
                            });

                    sub_port.getTracert().getValues().setEnergy(
                            sub_port.getTracert().getValues().getEnergy() +
                                    line_port.getTracert().getValues().getEnergy());
                    sub_data.getTracert().getValues().setEnergy(sub_port.getTracert().getValues().getEnergy());
                });

        return sub_data.getTracert().getValues().getEnergy();
    }

    @Override
    public void run() {
        logger.info("Игровой сценарий для {} запущен", task.getPowerSystem().getIdenty());

        // останов всех устройств стенда
        standAllOff();

        double secInMillis = getSecondInMillis(); // реальных секунд в мс
        long gameStep = Math.round(MODEL_DISCRET / secInMillis); // дискретизация - шаг игры в секундах
        long delay = Math.round((gameStep * secInMillis) / 10.0) * 10L; // дискретизация - в мс

        Map<Integer, PortTracertInternalData> ports = buildPorts();
        Map<Integer, HubTracertInternalData> hubs = buildHubs(SECONDS_OF_DAY / gameStep, gameStep);
        Map<Integer, ElectricalSubnet> lines = buildLines();

        int totalSec = 0;
        GameDataset dataset = new GameDataset(task.getPowerSystem().getDevaddr());
        dataset.setRoot_values(new HubTracertValues(task.getGameBlock().getRoot().getAddress()));
        GameValues val = new GameValues();
        val.setCredit(task.getGameBlock().getCredit_total());
        dataset.setCumulative_total(val);
        dataset.setPort_values(ports.values()
                        .stream()
                        .map(PortTracertInternalData::getTracert)
                        .toArray(PortTracertValues[]::new)
        );
        dataset.setHub_values(hubs.values()
                .stream()
                .map(HubTracertInternalData::getTracert)
                .toArray(HubTracertValues[]::new)
        );

        messenger.gameTracertMessage(null, dataset);

        try {
            Thread.sleep(500); // немного притормозим перед началом...

            int fNumber = 0;
            double energy_for_step = ((double) gameStep) / ((double) SECONDS_OF_HOURS);

            while(totalSec <= SECONDS_OF_DAY) {

                Thread.sleep(delay);

                // очистка мгновенных значений
                dataset.getRoot_values().setValues(new GameValues());
                hubs.values().forEach(e -> e.getTracert().setValues(new GameValues()));
                ports.values().forEach(e -> e.getTracert().setValues(new GameValues()));

                // расчет мощностей генерации
                double ext_energy = ((MainSubstationPowerSystem) task.getGameBlock().getRoot().getOwner())
                        .getData().getExternal_energy();
                int finalFNumber = fNumber;
                Arrays.stream(task.getGameBlock().getRoot().getInputs())
                        .filter(e -> e.getConnections() != null)
                        .forEach(line -> {
                            PortTracertInternalData line_port = ports.get(line.getAddress());
                            PortTracertInternalData gen_port = ports.get(line.getConnections()[0].getAddress());
                            HubTracertInternalData gen_data = hubs.get(gen_port.getPort().getOwner().getAddress());
                            double gen_energy = 0.0;
                            double gen_reserve = 0.0;
                            double v;

                            // мощности генераторов
                            switch (gen_data.getOes().getComponentType()) {
                                case STORAGE -> {
                                    EnergyStorageSpecification data = ((EnergyStorage) gen_data.getOes()).getData();
                                    v = data.getEnergy();
                                    if (gen_port.getPort().isOn()) {
                                        gen_port.getTracert().getValues().setEnergy(v);
                                        gen_energy = v;
                                    } else if (data.getMode() == GenerationUsageModes.RESERVE) {
                                        gen_reserve = v;
                                    }
                                }
                                case GENERATOR -> {
                                    if (gen_data.useForecast()) {
                                        v = gen_data.getForecast()[finalFNumber];
                                    } else {
                                        v = ((Generation) gen_data.getOes()).getData().getEnergy();
                                    }
                                    v *= energy_for_step;
                                    gen_data.getTracert().getValues().setGeneration(v);
                                    if (gen_port.getPort().isOn()) {
                                        gen_port.getTracert().getValues().setGeneration(v);
                                        gen_energy = v;
                                    } else if (((Generation) gen_data.getOes()).getData().getMode() == GenerationUsageModes.RESERVE) {
                                        gen_reserve = v;
                                    }
                                }
                                case GREEGENERATOR -> {
                                    GreenGenerationSpecification data = ((GreenGeneration) gen_data.getOes()).getData();
                                    v = data.getEnergy() * (modelingData.getGreenGeneration(gen_data.getHub().getAddress()) / 100.0);
                                    v *= energy_for_step;
                                    if (gen_port.getPort().isOn()) {
                                        gen_port.getTracert().getValues().setEnergy(v);
                                        gen_energy = v;
                                    } else if (data.getMode() == GenerationUsageModes.RESERVE) {
                                        gen_reserve = v;
                                    }
                                }
                            }

                            if (line.isOn()) { // линия подключена
                                ElectricalSubnet subnet = lines.get(line.getAddress());
                                line_port.getTracert().getValues().setGeneration(gen_energy * subnet.getData().getLossfactor());
                                line_port.getTracert().getValues().setReserve_generation(gen_reserve * subnet.getData().getLossfactor());
                            }

                            dataset.getRoot_values().getValues().setGeneration(
                                    dataset.getRoot_values().getValues().getGeneration() + line_port.getTracert().getValues().getGeneration());
                            dataset.getRoot_values().getValues().setReserve_generation(
                                    dataset.getRoot_values().getValues().getReserve_generation() + line_port.getTracert().getValues().getReserve_generation());
                        });
                dataset.getRoot_values().getValues().setGeneration(
                        dataset.getRoot_values().getValues().getGeneration() + (ext_energy * energy_for_step));

                // расчет потребления
                Arrays.stream(task.getGameBlock().getRoot().getOutputs())
                        .filter(e -> e.getConnections() != null)
                        .filter(IConnectionPort::isOn)
                        .forEach(line -> {
                            PortTracertInternalData line_port = ports.get(line.getAddress());

                            Arrays.stream(line.getConnections())
                                    .filter(IConnectionPort::isOn)
                                    .forEach(conn -> {
                                        PortTracertInternalData consumer_port = ports.get(conn.getAddress());
                                        HubTracertInternalData consumer_data = hubs.get(consumer_port.getPort().getOwner().getAddress());
                                        double v = 0.0;
                                        double v1 = 0.0;

                                        if (consumer_data.getOes().getComponentType() == SupportedTypes.DISTRIBUTOR) {
                                            v1 = getSubsystemEnergy(consumer_data, consumer_port,
                                                    ports, hubs, finalFNumber, energy_for_step);
                                        } else {
                                            if (consumer_data.useForecast()) {
                                                v = consumer_data.getForecast()[finalFNumber];
                                            } else {
                                                v = ((Consumer) consumer_data.getOes()).getData().getEnergy();
                                            }
                                        }

                                        v *= energy_for_step;
                                        v += v1;
                                        consumer_port.getTracert().getValues().setEnergy(v);
                                        consumer_data.getTracert().getValues().setEnergy(v);
                                        line_port.getTracert().getValues().setEnergy(
                                                line_port.getTracert().getValues().getEnergy() +
                                                        consumer_data.getTracert().getValues().getEnergy());
                                    });

                            dataset.getRoot_values().getValues().setEnergy(
                                    dataset.getRoot_values().getValues().getEnergy() +
                                            line_port.getTracert().getValues().getEnergy());
                        });

                // расчет экологии

                // money

                totalSec += gameStep;
                fNumber++;

                dataset.setHub_values(hubs.values()
                        .stream()
                        .map(HubTracertInternalData::getTracert)
                        .toArray(HubTracertValues[]::new));
                dataset.setPort_values(ports.values()
                        .stream()
                        .map(PortTracertInternalData::getTracert)
                        .toArray(PortTracertValues[]::new));

                dataset.setSeconds(totalSec);
                messenger.gameTracertMessage(null, dataset);

            }

            Thread.sleep(500); // приторможу перед завершением ...
        }
        catch (InterruptedException ignored) {
            logger.warn("Игровой сценарий для {} прерван", task.getPowerSystem().getIdenty());
        }

        // останов всех устройств стенда
        standAllOff();

        logger.info("Игровой сценарий для {} завершен", task.getPowerSystem().getIdenty());
    }
}

