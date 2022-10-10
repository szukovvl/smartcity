package re.smartcity.modeling;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import re.smartcity.common.resources.Messages;
import re.smartcity.energynet.IComponentIdentification;
import re.smartcity.energynet.IGeneration;
import re.smartcity.energynet.SupportedConsumers;
import re.smartcity.energynet.SupportedTypes;
import re.smartcity.energynet.component.Consumer;
import re.smartcity.energynet.component.data.ConsumerSpecification;
import re.smartcity.modeling.data.StandBinaryPackage;
import re.smartcity.modeling.scheme.IConnectionPort;
import re.smartcity.modeling.scheme.IOesHub;
import re.smartcity.modeling.scheme.OesRootHub;
import re.smartcity.modeling.scheme.OesUnknownHub;
import re.smartcity.stand.SerialElementAddresses;
import re.smartcity.stand.SerialPackageBuilder;

import java.util.*;

import static re.smartcity.stand.SerialElementAddresses.*;
import static re.smartcity.stand.SerialServiceSymbols.SEQUENCE_SEPARATOR;

public class oesSchemeMonitor implements Runnable {

    private final Logger logger = LoggerFactory.getLogger(oesSchemeMonitor.class);

    private final Object _syncThread;
    private final Queue<StandBinaryPackage> scheme;
    private final ModelingData modelingData;

    public oesSchemeMonitor(ModelingData modelingData, Object syncThread, Queue<StandBinaryPackage> scheme) {
        this._syncThread = syncThread;
        this.scheme = scheme;
        this.modelingData = modelingData;
    }

    private Byte[][] parsePackage(StandBinaryPackage pack) {
        List<Byte[]> items = new ArrayList<>();
        List<Byte> tail = new ArrayList<>();
        for (Byte datum : pack.getData()) {
            if (datum == SEQUENCE_SEPARATOR) {
                items.add(tail.toArray(Byte[]::new));
                tail = new ArrayList<>();
            } else {
                tail.add(datum);
            }
        }
        items.add(tail.toArray(Byte[]::new));
        return items.toArray(Byte[][]::new);
    }

    private String combineErrorMsg(String msg, String appendMsg) {
        if (msg != null) {
            return String.format("%s %s", msg, appendMsg);
        }
        return appendMsg;
    }

    /*
    private void buildDistributor(StandBinaryPackage pack) {
        // подключается только к главной подстанции
        // на выходы подключаются только потребители 3-й категории

        // 1. ищу компонент, куда подключено
        IComponentIdentification oesOwner = Arrays.stream(Arrays.stream(pack.getOesbin())
                .filter(e -> Arrays.stream(e)
                        .anyMatch(b -> b == ((EnergyDistributor) pack.getOes()).getData().getInaddr()))
                .findFirst()
                .get())
                .filter(b -> b != ((EnergyDistributor) pack.getOes()).getData().getInaddr())
                .map(this::findOesComponent)
                .findFirst()
                .orElse(null);

        if (oesOwner != null) {
            // 2. ищу хаб компонента-владельца
        }

    }*/

    /*private void buildConsumer(StandBinaryPackage pack) {
        // запитывается только с одной главной подстанции
        // подключается только к выходам главной подстанции
        // подключается только к разным выходам
    }*/

    private void buildConnections(StandBinaryPackage pack, List<IOesHub> devices, IConnectionPort[] ports) {
        Arrays.stream(ports)
                .forEach(e -> {
                    // получаю только адреса подключенных устройств
                    Byte[] items = Arrays.stream(Arrays.stream(pack.getOesbin())
                                    .filter(l -> Arrays.stream(l)
                                            .anyMatch(b -> b == e.getAddress()))
                                    .findFirst()
                                    .orElse(new Byte[0]))
                            .filter(b -> b != e.getAddress())
                            .toArray(Byte[]::new);
                    logger.info("-- порт {}: [{}]", String.format("%02X", e.getAddress()),
                            SerialPackageBuilder.bytesAsHexString(items));

                    // ищу компоненты по полученным адресам и ранее подключенные устройства
                    if (items.length != 0) {
                        Arrays.stream(items)
                                .forEach(b -> { // b - адрес устройства на линии
                                    IOesHub hub = devices.stream()
                                            .filter(a -> a.itIsMine(b)) // ищу у себя
                                            .findFirst()
                                            .orElse(null);
                                    if (hub == null) { // устройства нет
                                        hub = Arrays.stream(
                                                        pack.getTask().getRoot().getDevices() != null
                                                                ? pack.getTask().getRoot().getDevices()
                                                                : new IOesHub[0]
                                                )
                                                // возможно уже использовался?
                                                .filter(a -> a.itIsMine(b))
                                                .findFirst()
                                                .orElse(
                                                        // если нет - ищу сам компонент
                                                        Arrays.stream(modelingData.getAllobjects())
                                                                .filter(a -> a.itIsMine(b))
                                                                .findFirst()
                                                                .map(OesRootHub::createOther)
                                                                .orElse(null)
                                                );
                                        if (hub != null) {
                                            // подключить объект
                                            if (!e.addConection(hub.connectionByAddress(b))) {
                                                // подключение не добавлено
                                                e.setError(combineErrorMsg(
                                                        e.getError(), String.format(Messages.FSER_1, b)));
                                                logger.warn(e.getError()); // !!!
                                            }
                                            devices.add(hub);
                                        } else {
                                            // такого объекта нет
                                            e.setError(combineErrorMsg(
                                                    e.getError(), String.format(Messages.FSER_0, b)));
                                            logger.warn(e.getError()); // !!!
                                            if (!e.addConection(OesUnknownHub.create(b).connectionByAddress(b))) {
                                                // подключение не добавлено
                                                e.setError(combineErrorMsg(
                                                        e.getError(), String.format(Messages.FSER_1, b)));
                                                logger.warn(e.getError()); // !!!
                                            }
                                        }
                                    } else {
                                        // нашел у себя!
                                        if (!e.addConection(hub.connectionByAddress(b))) {
                                            // подключение не добавлено
                                            e.setError(combineErrorMsg(
                                                    e.getError(), String.format(Messages.FSER_1, b)));
                                            logger.warn(e.getError()); // !!!
                                        }
                                    }
                                });
                    } else {
                        // ничего нет
                        e.setConnections(null);
                    }

                    logger.info("-- объекты порта {}: {}",
                            String.format("%02X", e.getAddress()), e.getConnections());
                });
    }

    private void buildDevices(List<IOesHub> devices, IConnectionPort[] ports) {
        // дополняю список устройств от текущего
        Arrays.stream(ports)
                .forEach(e -> Arrays.stream(e.getConnections() != null
                                ? e.getConnections()
                                : new IConnectionPort[0])// перебираю все подключенные устройства к порту
                        .map(IConnectionPort::getOwner) // теперь идут обертки компонент
                        .filter(IOesHub::hasOwner) // должен быть сопоставлен с компонентом
                        .filter(c -> devices.stream() // далее идут устройства, которых нет в дополняемом списке
                                .noneMatch(a -> a.getAddress() == c.getAddress()))
                        .forEach(c -> {
                            devices.add(c);
                            if (c.supportOutputs() && (c.getOwner().getComponentType() == SupportedTypes.DISTRIBUTOR)) {
                                // упрощаю, беру только с поддержкой выходов и миниподстанции
                                // считаю, что потребители должны определиться при переборе выходов др. устройств
                                buildDevices(devices, c.getOutputs());
                            }
                        }));
    }

    private void rootOesChanged(StandBinaryPackage pack) {
        logger.info("--= {} =--", String.format("%02X", pack.getDevaddr())); // !!!
        // !!! не отслеживаю количество подключений - считаю их неизменными.
        OesRootHub root = OesRootHub.create(pack.getTask().getPowerSystem());
        List<IOesHub> devices = new ArrayList<>();

        // 1. проверка подключения к блоку управления
        Byte[] block = Arrays.stream(pack.getOesbin())
                .filter(e -> Arrays.stream(e)
                        .anyMatch(SerialElementAddresses::isControlBlock))
                .findFirst()
                .map(b -> Arrays.stream(b)
                        .filter(a -> !SerialElementAddresses.isControlBlock(a))
                        .toArray(Byte[]::new))
                .orElse(null);
        if (block == null || block.length == 0) {
            pack.getTask().getRoot().setError(
                    combineErrorMsg(pack.getTask().getRoot().getError(), Messages.SER_0));
            logger.warn(pack.getTask().getRoot().getError()); // !!!
        } else if (Arrays.stream(block)
                .noneMatch(b -> b == pack.getTask().getRoot().getControlPort().getAddress())) {
            pack.getTask().getRoot().setError(
                    combineErrorMsg(pack.getTask().getRoot().getError(), Messages.SER_2));
            logger.warn(pack.getTask().getRoot().getError()); // !!!
        }

        // 2. сборка входных линий
        buildConnections(pack, devices, pack.getTask().getRoot().getInputs());

        // проверки на допустимость подключений
        Arrays.stream(pack.getTask().getRoot().getInputs())
                .forEach(e -> {
                    // должно быть не более одного объекта генерации
                    if (e.getConnections() != null) {
                        if (e.getConnections().length > 1 ||
                                (!e.getConnections()[0].getOwner().hasOwner()) ||
                                (e.getConnections()[0].getOwner().hasOwner() &&
                                        (!e.getConnections()[0].getOwner().getClass().isAssignableFrom(IGeneration.class)))) {
                            e.setError(
                                    combineErrorMsg(e.getError(), Messages.SER_1));
                            logger.warn(e.getError()); // !!!
                        }
                    }
                });

        // 3. сборка выходных линий
        buildConnections(pack, devices, pack.getTask().getRoot().getOutputs());

        // проверки на допустимость подключений
        Arrays.stream(pack.getTask().getRoot().getInputs())
                .forEach(e -> {
                    // могут подключаться только миниподстанции и потребители 1-й и 2-й категорий
                    if (!Arrays.stream(
                            e.getConnections() != null ? e.getConnections() : new IConnectionPort[0])
                            .allMatch(c -> {
                                if (c.getOwner().hasOwner()) {
                                    switch (c.getOwner().getOwner().getComponentType()) {
                                        case DISTRIBUTOR -> { return true; }
                                        case CONSUMER -> {
                                            ConsumerSpecification pars = ((Consumer) c.getOwner().getOwner()).getData();
                                            return pars.getConsumertype() == SupportedConsumers.HOSPITAL ||
                                                    pars.getConsumertype() == SupportedConsumers.INDUSTRY;
                                        }
                                        default -> { return false; }
                                    }
                                } else {
                                    return false;
                                }
                            })) {
                        e.setError(
                                combineErrorMsg(e.getError(), Messages.SER_3));
                        logger.warn(e.getError()); // !!!
                    }

                    // одно и то же устройство не может быть подключено более одного раза
                    Set<Integer> items = new HashSet<>();
                    if (Arrays.stream(
                            e.getConnections() != null ? e.getConnections() : new IConnectionPort[0])
                            .filter(c -> c.getOwner().hasOwner())
                            .map(c -> c.getOwner().getAddress())
                            .anyMatch(n -> !items.add(n))) {
                        e.setError(
                                combineErrorMsg(e.getError(), Messages.SER_4));
                        logger.warn(e.getError()); // !!!
                    }
                });

        logger.info(":: --= {}/{} =--", String.format("%02X", pack.getDevaddr()), devices); // !!!

        // пополнить список своих устройств
        // актуально только для выходных линий
        buildDevices(devices, pack.getTask().getRoot().getOutputs());

        logger.info(":: --= {}/{} =--", String.format("%02X", pack.getDevaddr()), devices); // !!!

        root.setDevices(devices.size() != 0 ? devices.toArray(IOesHub[]::new) : null);
        pack.getTask().setRoot(root);
    }

    private void checkAndBuild(StandBinaryPackage pack) {
        if (pack.getTask() != null) {
            rootOesChanged(pack);
        } else if (pack.getOes().getComponentType() == SupportedTypes.DISTRIBUTOR) {
            // это или миниподстанция или потребитель 1, 2-й категорий
            // думаю, что вес имеют только миниподстанции, соответственно, потребителей скидываю...

        }
    }

    @Override
    public void run() {
        logger.info("Поток обслуживания схемы модели запущен.");
        int errorCount = 0;
        while (true) {
            try {
                StandBinaryPackage pack = scheme.poll();
                if (pack == null) {
                    synchronized (_syncThread) {
                        _syncThread.wait();
                    }
                    continue;
                }

                logger.info(String.format("обработка пакета схемы %02X: %s", pack.getDevaddr(),
                        SerialPackageBuilder.bytesAsHexString(pack.getData())));
                switch (pack.getDevaddr()) {
                    case CONTROL_BLOCK: continue;
                    case MAIN_SUBSTATION_1:
                    case MAIN_SUBSTATION_2:
                        pack.setTask(Arrays.stream(modelingData.getTasks())
                                .filter(e -> e.getPowerSystem().getDevaddr() == pack.getDevaddr())
                                .findFirst()
                                .get());
                        pack.setOesbin(parsePackage(pack));
                        break;
                    default:
                        IComponentIdentification cmp = Arrays.stream(modelingData.getAllobjects())
                                .filter(e -> e.getDevaddr() == pack.getDevaddr())
                                .findFirst()
                                .orElse(null);
                        if (cmp == null) {
                            logger.warn("неизвестный объект {}", pack.getDevaddr());
                            continue;
                        }
                        switch (cmp.getComponentType()) {
                            case CONSUMER -> {
                                if (((Consumer) cmp).getData().getConsumertype() == SupportedConsumers.DISTRICT) {
                                    continue;
                                }
                                pack.setOes(cmp);
                                pack.setOesbin(parsePackage(pack));
                            }
                            case DISTRIBUTOR -> {
                                pack.setOes(cmp);
                                pack.setOesbin(parsePackage(pack));
                            }
                            default -> {
                                continue;
                            }
                        }
                }

                logger.info(String.format("- устройство %02X:", pack.getDevaddr()));
                Arrays.stream(pack.getOesbin()).forEach(e -> logger.info("- {}", SerialPackageBuilder.bytesAsHexString(e)));

                /*
                Для дальнейшей обработки используются только главные подстанции, миниподстанции и 2-х входовые потребители;
                построение основного узла выполняется от главной подстанции;
                построение дополнительных узлов выполняется от миниподстанции, также выполняется проверка подключения входа;
                для потребителей выполняется только проверка фактического подключения.

                Все остальные объекты игнорируются.
                 */

                checkAndBuild(pack);
                errorCount = 0;
            }
            catch (InterruptedException ignored) {
                break;
            }
            catch (Exception ex) {
                logger.error(ex.getMessage());
                errorCount++;
                if (errorCount > 5) {
                    errorCount = 0;
                    logger.warn("Любое действие ведет к ошибке");
                }
            }
        }
        logger.warn("Поток обслуживания схемы модели остановлен.");
    }
}
