package re.smartcity.modeling;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import re.smartcity.common.resources.Messages;
import re.smartcity.energynet.IComponentIdentification;
import re.smartcity.energynet.SupportedConsumers;
import re.smartcity.energynet.SupportedTypes;
import re.smartcity.energynet.component.Consumer;
import re.smartcity.energynet.component.EnergyDistributor;
import re.smartcity.energynet.component.data.ConsumerSpecification;
import re.smartcity.energynet.component.data.EnergyDistributorSpecification;
import re.smartcity.modeling.data.StandBinaryPackage;
import re.smartcity.modeling.scheme.ComponentOesHub;
import re.smartcity.modeling.scheme.IControlHub;
import re.smartcity.stand.SerialElementAddresses;
import re.smartcity.stand.SerialPackageBuilder;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

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

    private IComponentIdentification findOesComponent(byte devaddr) {
        return Arrays.stream(this.modelingData.getTasks())
                .map(TaskData::getPowerSystem)
                .filter(e -> e.getDevaddr() == devaddr ||
                        e.getData().getCtrladdr() == devaddr ||
                        Arrays.stream(e.getData().getInputs())
                                .anyMatch(a -> a.getDevaddr() == devaddr) ||
                        Arrays.stream(e.getData().getOutputs())
                                .anyMatch(a -> a.getDevaddr() == devaddr))
                .map(IComponentIdentification.class::cast)
                .findFirst()
                .orElse(Arrays.stream(this.modelingData.getAllobjects())
                        .filter(e -> {
                            if (e.getDevaddr() != devaddr) {
                                switch (e.getComponentType()) {
                                    case DISTRIBUTOR: {
                                        EnergyDistributorSpecification data = ((EnergyDistributor) e).getData();
                                        return data.getInaddr() == devaddr ||
                                                Arrays.stream(data.getOutputs())
                                                        .anyMatch(b -> b.getDevaddr() == devaddr);
                                    }
                                    case CONSUMER: {
                                        ConsumerSpecification data = ((Consumer) e).getData();
                                        return Arrays.stream(data.getInputs())
                                                .anyMatch(b -> b.getDevaddr() == devaddr);
                                    }
                                    default: return false;
                                }
                            }
                            return true;
                        })
                        .findFirst()
                        .orElse(null));
    }

    private boolean isGenerationType(IComponentIdentification oes) {
        return oes.getComponentType() == SupportedTypes.GENERATOR ||
                oes.getComponentType() == SupportedTypes.STORAGE ||
                oes.getComponentType() == SupportedTypes.GREEGENERATOR;
    }

    private boolean isConsumerType_A(IComponentIdentification oes) {
        return oes.getComponentType() == SupportedTypes.DISTRIBUTOR ||
                (oes.getComponentType() == SupportedTypes.CONSUMER &&
                        ((Consumer) oes).getData().getConsumertype() != SupportedConsumers.DISTRICT);
    }

    private boolean isMainStationOutputs(byte devaddr) {
        return Arrays.stream(this.modelingData.getTasks())
                .anyMatch(e -> Arrays.stream(e.getRoot().getOutputs())
                        .anyMatch(b -> Arrays.stream(b.getItems())
                                .anyMatch(a -> a.getDevaddr() == devaddr)));
    }

    private IControlHub findInTree(byte devaddr) {
        IControlHub res = Arrays.stream(this.modelingData.getTasks())
                .map(TaskData::getRoot)
                .filter(e -> e.getDevaddr() == devaddr) // может это главная подстанция?
                .findFirst()
                .orElse(/*Arrays.stream(this.modelingData.getTasks())
                        .map(e -> e.getRoot().getOutputs())
                        ); // нет, не главная подстанция, обхожу выходы
                        */
                null);

        return res;
    }

    private void buildRoot(StandBinaryPackage pack) {
        logger.info("--= {} =--", String.format("%02X", pack.getDevaddr())); // !!!
        // !!! не отслеживаю количество подключений - считаю их неизменными.
        // Сброс возможной установленной ошибки.
        pack.getTask().getRoot().setErrorMsg(null);
        Arrays.stream(pack.getTask().getRoot().getInputs())
                .forEach(e -> e.setErrorMsg(null));
        Arrays.stream(pack.getTask().getRoot().getOutputs())
                .forEach(e -> e.setErrorMsg(null));

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
            pack.getTask().getRoot().setErrorMsg(
                    combineErrorMsg(pack.getTask().getRoot().getErrorMsg(), Messages.SER_0));
            logger.warn(pack.getTask().getRoot().getErrorMsg()); // !!!
        } else if (Arrays.stream(block)
                .noneMatch(b -> b == pack.getTask().getRoot().getCtrladdr())) {
            pack.getTask().getRoot().setErrorMsg(
                    combineErrorMsg(pack.getTask().getRoot().getErrorMsg(), Messages.SER_2));
            logger.warn(pack.getTask().getRoot().getErrorMsg()); // !!!
        }

        // 2. сборка входных линий
        Arrays.stream(pack.getTask().getRoot().getInputs())
                .forEach(e -> {
                    // получаю только адреса подключенных устройств
                    Byte[] items = Arrays.stream(Arrays.stream(pack.getOesbin())
                                    .filter(l -> Arrays.stream(l)
                                            .anyMatch(b -> b == e.getDevaddr()))
                                    .findFirst()
                                    .get())
                            .filter(b -> b != e.getDevaddr())
                            .toArray(Byte[]::new);
                    logger.info("-- (!) {}: [{}]", String.format("%02X", e.getDevaddr()),
                            SerialPackageBuilder.bytesAsHexString(items));

                    // ищу ссылки на компоненты по полученным адресам и ранее подключенные устройства
                    if (items.length != 0) {
                        List<IControlHub> newitems = new ArrayList<>();
                        Arrays.stream(items)
                                .forEach(b -> { // обхожу по новым
                                    IControlHub hub = Arrays.stream(e.getItems() != null ?
                                                    e.getItems() : new IControlHub[0])
                                            .filter(a -> a.getDevaddr() == b)
                                            .findFirst()
                                            .orElse(new ComponentOesHub(findOesComponent(b), b));
                                    newitems.add(hub);
                                    if (hub.getLinkedOes() == null) {
                                        e.setErrorMsg(combineErrorMsg(
                                                e.getErrorMsg(), String.format(Messages.FSER_0, b)));
                                        logger.warn(e.getErrorMsg()); // !!!
                                    }
                                });
                        // на входной линии может быть только одно генерирующее устройство
                        e.setItems(newitems.toArray(IControlHub[]::new));
                        if (e.getItems().length > 1 || Arrays.stream(e.getItems())
                                .anyMatch(b -> b.getLinkedOes() == null || !isGenerationType(b.getLinkedOes()))) {
                            e.setErrorMsg(combineErrorMsg(
                                    e.getErrorMsg(), Messages.SER_1));
                            logger.warn(e.getErrorMsg()); // !!!
                        }
                    } else {
                        // ничего нет
                        e.setItems(null);
                    }
                    logger.info("-- объекты линии {}: {}",
                            String.format("%02X", e.getDevaddr()), e.getItems());
                });

        // 3. сборка выходных линий
        Arrays.stream(pack.getTask().getRoot().getOutputs())
                .forEach(e -> {
                    // получаю только адреса подключенных устройств
                    Byte[] items = Arrays.stream(Arrays.stream(pack.getOesbin())
                                    .filter(l -> Arrays.stream(l)
                                            .anyMatch(b -> b == e.getDevaddr()))
                                    .findFirst()
                                    .get())
                            .filter(b -> b != e.getDevaddr())
                            .toArray(Byte[]::new);
                    logger.info("-- {}: [{}]", String.format("%02X", e.getDevaddr()),
                            SerialPackageBuilder.bytesAsHexString(items));

                    // ищу ссылки на компоненты по полученным адресам и ранее подключенные устройства
                    if (items.length != 0) {
                        List<IControlHub> newitems = new ArrayList<>();
                        Arrays.stream(items)
                                .forEach(b -> { // обхожу по новым
                                    IControlHub hub = Arrays.stream(e.getItems() != null ?
                                                    e.getItems() : new IControlHub[0])
                                            .filter(a -> a.getDevaddr() == b)
                                            .findFirst()
                                            .orElse(new ComponentOesHub(findOesComponent(b), b));
                                    newitems.add(hub);
                                    if (hub.getLinkedOes() == null) {
                                        e.setErrorMsg(combineErrorMsg(
                                                e.getErrorMsg(), String.format(Messages.FSER_0, b)));
                                        logger.warn(e.getErrorMsg()); // !!!
                                    }
                                });
                        // на выходной линии могут быть только одно потребители
                        e.setItems(newitems.toArray(IControlHub[]::new));
                        if (Arrays.stream(e.getItems())
                                .anyMatch(b -> b.getLinkedOes() == null || !isConsumerType_A(b.getLinkedOes()))) {
                            e.setErrorMsg(combineErrorMsg(
                                    e.getErrorMsg(), Messages.SER_3));
                            logger.warn(e.getErrorMsg()); // !!!
                        }
                        // один элемент не может быть дважды подключен к одному выходу
                        if (Arrays.stream(e.getItems())
                                .map(b -> b.getLinkedOes() != null ? b.getLinkedOes().getDevaddr() : b.getDevaddr())
                                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
                                .values()
                                .stream()
                                .anyMatch(a -> a > 1)) {
                            e.setErrorMsg(combineErrorMsg(
                                    e.getErrorMsg(), Messages.SER_4));
                            logger.warn(e.getErrorMsg()); // !!!
                        }
                    } else {
                        // ничего нет
                        e.setItems(null);
                    }
                    logger.info("-- объекты линии {}: {}",
                            String.format("%02X", e.getDevaddr()), e.getItems());
                });

        logger.info(":: --= {} =--", String.format("%02X", pack.getDevaddr())); // !!!
    }

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

    }

    private void buildConsumer(StandBinaryPackage pack) {
        // запитывается только с одной главной подстанции
        // подключается только к выходам главной подстанции
        // подключается только к разным выходам
    }

    private void checkAndBuild(StandBinaryPackage pack) {
        if (pack.getTask() != null) {
            buildRoot(pack);
        } else {
            // это или миниподстанция или потребитель 1, 2-й категорий
            if (pack.getOes().getComponentType() == SupportedTypes.DISTRIBUTOR) {
                buildDistributor(pack);
            } else {
                buildConsumer(pack);
            }
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
