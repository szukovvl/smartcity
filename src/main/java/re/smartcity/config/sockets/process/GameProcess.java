package re.smartcity.config.sockets.process;

import io.r2dbc.spi.Parameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import re.smartcity.config.sockets.GameSocketHandler;
import re.smartcity.energynet.IComponentIdentification;
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

    @Autowired
    private StandService standService;

    @Autowired
    private WindRouterHandlers wind;

    public final static int SECONDS_OF_DAY = 86400; // количество секунд в сутках
    public final static int MODEL_DISCRET = 250; // время дискретизации в мс

    private final Logger logger = LoggerFactory.getLogger(GameProcess.class);

    private final TaskData task;
    private final GameSocketHandler messenger;
    private final ModelingData modelingData;

    public GameProcess(TaskData task, GameSocketHandler messenger, ModelingData modelingData) {
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
                .filter(e -> e.supportOutputs())
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

    private Map<Integer, HubTracertInternalData> buildHubs() {
        Map<Integer, HubTracertInternalData> hubs = new HashMap<>();

        Arrays.stream(task.getGameBlock().getRoot().getDevices())
                .forEach(e -> {
                    IComponentIdentification cmp = Arrays.stream(modelingData.getAllobjects())
                            .filter(oes -> oes.getDevaddr() == e.getAddress())
                            .findFirst()
                            .orElseThrow();
                    hubs.put(e.getAddress(),
                            new HubTracertInternalData(e, cmp));

                    // установить прогноз текущего сценария (потребители и ДГ)

                });

        return hubs;
    }

    private void standAllOff() {
        wind.windOff();
        standService.pushSerialCommand(
                new SerialCommand(SerialElementAddresses.SUN_SIMULATOR,
                        SerialPackageTypes.SET_BRIGHTNESS_SUN_SIMULATOR, 0));
        standService.pushSerialCommand(
                new SerialCommand(SerialPackageTypes.SET_HIGHLIGHT_LEVEL, 0));
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
        Map<Integer, HubTracertInternalData> hubs = buildHubs();

        int totalSec = 0;
        GameDataset dataset = new GameDataset(task.getPowerSystem().getDevaddr());
        dataset.setRoot_values(HubTracertValues.builder()
                .hub(task.getGameBlock().getRoot().getAddress())
                .build());
        dataset.setCumulative_total(GameValues.builder()
                .credit(task.getGameBlock().getCredit_total())
                .build());
        dataset.setPort_values(ports.values()
                        .stream()
                        .map(e -> e.getTracert())
                        .toArray(PortTracertValues[]::new)
        );
        dataset.setHub_values(hubs.values()
                .stream()
                .map(e -> e.getTracert())
                .toArray(HubTracertValues[]::new)
        );

        messenger.gameTracertMessage(null, dataset);

        try {
            Thread.sleep(500); // немного притормозим перед началом...

            while(totalSec < SECONDS_OF_DAY) {
                Thread.sleep(delay);
                totalSec += gameStep;

                dataset.setRoot_values(HubTracertValues.builder()
                        .hub(task.getGameBlock().getRoot().getAddress())
                        .totals(GameValues.builder()
                                .energy(dataset.getRoot_values().getTotals().getEnergy() + 1.0)
                                .build())
                        .build());

                ports.values().forEach(e -> e.setTracert(PortTracertValues.builder()
                        .on(e.getTracert().isOn())
                        .owner(e.getTracert().getOwner())
                        .state(e.getTracert().getState())
                        .port(e.getTracert().getPort())
                        .zone(e.getTracert().getZone())
                        .totals(GameValues.builder()
                                .energy(e.getTracert().getTotals().getEnergy() + 0.15)
                                .carbon(e.getTracert().getTotals().getCarbon() + 0.1)
                                .build())
                        .values(GameValues.builder()
                                .energy(e.getTracert().getValues().getEnergy() + 0.33)
                                .debit(e.getTracert().getValues().getDebit() + 10.2)
                                .build())
                        .build()));
                dataset.setPort_values(ports.values()
                        .stream()
                        .map(e -> e.getTracert())
                        .toArray(PortTracertValues[]::new)
                );

                hubs.values().forEach(e -> e.setTracert(HubTracertValues.builder()
                        .hub(e.getTracert().getHub())
                        .totals(GameValues.builder()
                                .carbon(e.getTracert().getTotals().getCarbon() + 0.05)
                                .build())
                        .values(GameValues.builder()
                                .debit(e.getTracert().getValues().getDebit() + 10.2)
                                .build())
                        .build()));
                dataset.setHub_values(hubs.values()
                        .stream()
                        .map(e -> e.getTracert())
                        .toArray(HubTracertValues[]::new));

                dataset.setSeconds(totalSec);
                messenger.gameTracertMessage(null, dataset);

            }
        }
        catch (InterruptedException ignored) {
            logger.warn("Игровой сценарий для {} прерван", task.getPowerSystem().getIdenty());
        }

        // останов всех устройств стенда
        standAllOff();

        logger.info("Игровой сценарий для {} завершен", task.getPowerSystem().getIdenty());
    }
}
