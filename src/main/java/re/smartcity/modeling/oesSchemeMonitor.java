package re.smartcity.modeling;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import re.smartcity.common.resources.Messages;
import re.smartcity.energynet.IComponentIdentification;
import re.smartcity.energynet.SupportedConsumers;
import re.smartcity.energynet.component.Consumer;
import re.smartcity.energynet.component.EnergyDistributor;
import re.smartcity.energynet.component.data.ConsumerSpecification;
import re.smartcity.energynet.component.data.EnergyDistributorSpecification;
import re.smartcity.modeling.data.StandBinaryPackage;
import re.smartcity.modeling.scheme.ComponentOesHub;
import re.smartcity.modeling.scheme.IControlHub;
import re.smartcity.modeling.scheme.PowerSystemHub;
import re.smartcity.stand.SerialElementAddresses;
import re.smartcity.stand.SerialPackageBuilder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Queue;

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
        if (Arrays.stream(pack.getOesbin())
                .filter(e -> Arrays.stream(e)
                        .anyMatch(SerialElementAddresses::isControlBlock))
                .findFirst()
                .isEmpty()) {
            pack.getTask().getRoot().setErrorMsg(
                    combineErrorMsg(pack.getTask().getRoot().getErrorMsg(), Messages.SER_0));
            logger.warn(pack.getTask().getRoot().getErrorMsg()); // !!!
        }

        // 2. сборка входных линий
        Arrays.stream(pack.getTask().getRoot().getInputs())
                .forEach(e -> {
                    // получаю только адреса подключенных устройств

                    // !!! ПРОВЕРИТЬ ИМЕННО КУДА ПОДКЛЮЧЕН БУ

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

                    // !!! ПРОВЕРИТЬ ДОПУСТИМОСТЬ ПОДКЛЮЧЕННОГО ЭЛЕМЕНТА

                    if (items.length != 0) {
                        List<IControlHub> newitems = new ArrayList<>();
                        Arrays.stream(items)
                                .forEach(b -> { // обхожу по новым
                                    IControlHub hub = Arrays.stream(e.getItems() != null ?
                                                    e.getItems() : new IControlHub[0])
                                            .filter(a -> a.getDevaddr() == b)
                                            .findFirst()
                                            .orElse(new ComponentOesHub(findOesComponent(b), b)); // ПЕРЕХВАТИТЬ NullPointerException (!?)
                                    if (hub != null) {
                                        newitems.add(hub);
                                    } else {
                                        logger.warn(Messages.FSER_0, b);
                                    }
                                });
                        e.setItems(newitems.toArray(IControlHub[]::new));
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
                                    if (hub != null) {
                                        newitems.add(hub);
                                    } else {
                                        logger.warn(Messages.FSER_0, b);
                                    }
                                });
                        e.setItems(newitems.toArray(IControlHub[]::new));
                    } else {
                        // ничего нет
                        e.setItems(null);
                    }
                    logger.info("-- объекты линии {}: {}",
                            String.format("%02X", e.getDevaddr()), e.getItems());
                });



        // проверка подключения к блоку управления

        // проверяю входные линии

        // проверя выходные линии
        /*Arrays.stream(pack.getTask().getRoot().getOutputs())
                .forEach(e -> {
                    logger.warn("-- линия: {}/{}", e.getDevaddr(), e);
                    Byte[] items = Arrays.stream(Arrays.stream(pack.getOesbin())
                                    .filter(l -> Arrays.stream(l)
                                            .filter(b -> b == e.getDevaddr())
                                            .findFirst()
                                            .isPresent())
                                    .findFirst()
                                    .get())
                            .filter(b -> b != e.getDevaddr())
                            .toArray(Byte[]::new);

                    // что подключено
                    for (Byte b : items) {
                        IComponentIdentification oes = Arrays.stream(this.modelingData.getAllobjects())
                                .filter(item -> {
                                    if (item.getDevaddr() == b) {
                                        return true;
                                    } else if (item.getComponentType() == SupportedTypes.DISTRIBUTOR) {
                                        return ((EnergyDistributor) item).getData().getInaddr() == b;
                                    } else if (item.getComponentType() == SupportedTypes.CONSUMER) {
                                        return Arrays.stream(((Consumer) item).getData().getInputs())
                                                .filter(l -> l.getDevaddr() == b)
                                                .findFirst()
                                                .isPresent();
                                    }
                                    return false;
                                })
                                .findFirst()
                                .orElse(null);
                        logger.info("-- подключен потребитель: {}", oes);
                    }
                });*/

        logger.info(":: --= {} =--", String.format("%02X", pack.getDevaddr())); // !!!
    }

    private void checkAndBuild(StandBinaryPackage pack) {
        if (pack.getTask() != null) {
            buildRoot(pack);
        }
        // !!!
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
                для дальнейшей обработки используются только главные подстанции, миниподстанции и 2-х входовые потребители;
                построение основного узла выполняется от главной подстанции;
                построение дополнительных узлов выполняется от миниподстанции, также выполняется проверка подключения входа;
                для потребителей выполняется только проверка фактического подключения.

                все остальные объекты игнорируются.
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
