package re.smartcity.modeling;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import re.smartcity.common.resources.Messages;
import re.smartcity.energynet.IComponentIdentification;
import re.smartcity.energynet.IGeneration;
import re.smartcity.energynet.SupportedConsumers;
import re.smartcity.energynet.SupportedTypes;
import re.smartcity.energynet.component.Consumer;
import re.smartcity.energynet.component.EnergyDistributor;
import re.smartcity.energynet.component.data.ConsumerSpecification;
import re.smartcity.energynet.component.data.EnergyDistributorSpecification;
import re.smartcity.modeling.data.StandBinaryPackage;
import re.smartcity.modeling.scheme.*;
import re.smartcity.stand.SerialElementAddresses;
import re.smartcity.stand.SerialPackageBuilder;

import java.util.*;
import java.util.stream.Stream;

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
                                        } else {
                                            // такого объекта нет
                                            hub = OesUnknownHub.create(b);
                                            e.setError(combineErrorMsg(
                                                    e.getError(), String.format(Messages.FSER_0, b)));
                                            logger.warn(e.getError()); // !!!
                                            if (!e.addConection(hub.connectionByAddress(b))) {
                                                // подключение не добавлено
                                                e.setError(combineErrorMsg(
                                                        e.getError(), String.format(Messages.FSER_1, b)));
                                                logger.warn(e.getError()); // !!!
                                            }
                                        }
                                        devices.add(hub);
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
                });
    }

    private void buildConnectionsAll(StandBinaryPackage pack, List<IOesHub> devices, IConnectionPort[] ports) {
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
                                        hub = Arrays.stream(modelingData.getTasks())
                                                .map(TaskData::getRoot)
                                                .filter(t -> t.getDevices() != null)
                                                .flatMap(t -> Stream.of(t.getDevices()))
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
                                        } else {
                                            // такого объекта нет
                                            hub = OesUnknownHub.create(b);
                                            e.setError(combineErrorMsg(
                                                    e.getError(), String.format(Messages.FSER_0, b)));
                                            logger.warn(e.getError()); // !!!
                                            if (!e.addConection(hub.connectionByAddress(b))) {
                                                // подключение не добавлено
                                                e.setError(combineErrorMsg(
                                                        e.getError(), String.format(Messages.FSER_1, b)));
                                                logger.warn(e.getError()); // !!!
                                            }
                                        }
                                        devices.add(hub);
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
                                // Упрощаю, беру только с поддержкой выходов и миниподстанции
                                // считаю, что потребители должны определиться при переборе выходов др. устройств
                                buildDevices(devices, c.getOutputs());
                            }
                        }));
        logger.info("-- buildDevices: {}", devices);
    }

    private void rebuildDeviceList(IConnectionPort[] ports, List<IOesHub> target, IOesHub newdev) {
        Arrays.stream(ports)
                .filter(port -> port.getConnections() != null)
                .flatMap(port -> Stream.of(port.getConnections())) // подключения на линии
                .map(IConnectionPort::getOwner)
                .filter(IOesHub::hasOwner) // обертка компонент
                .forEach(hub -> {
                    logger.info("++ rebuildDeviceList: {}", target);
                    if (target.stream().noneMatch(e -> e.getAddress() == hub.getAddress())) {
                        if (hub.getAddress() == newdev.getAddress()) {
                            target.add(newdev);
                        } else {
                            target.add(hub);
                        }
                    }
                    logger.info("++ (1) rebuildDeviceList: {}", target);
                    if (hub.getOwner().getComponentType() == SupportedTypes.DISTRIBUTOR) {
                        rebuildDeviceList(hub.getOutputs(), target, newdev);
                    }
                });
    }

    private void buildConnections_A(StandBinaryPackage pack, List<IOesHub> devices, IConnectionPort port) {

        //region 1. получаю только адреса подключенных устройств на линии
        Byte[] items = Arrays.stream(Arrays.stream(pack.getOesbin())
                        .filter(l -> Arrays.stream(l)
                                .anyMatch(b -> b == port.getAddress()))
                        .findFirst()
                        .orElse(new Byte[0]))
                .filter(b -> b != port.getAddress())
                .toArray(Byte[]::new);
        //endregion

        logger.info("-- порт {}: [{}]", String.format("%02X", port.getAddress()),
                SerialPackageBuilder.bytesAsHexString(items));

        //region 2. связываю компоненты
        Arrays.stream(items)
                .forEach(b -> { // b - адрес устройства на линии
                            // ищу в существующих
                    IOesHub hub = devices.stream()
                            .filter(a -> a.itIsMine(b))
                            .findFirst()
                            .orElse(null);
                    if (hub == null) { // устройства нет
                                // ищу сам компонент
                        hub = Arrays.stream(modelingData.getAllobjects())
                                .filter(a -> a.itIsMine(b))
                                .findFirst()
                                .map(OesRootHub::createOther)
                                .orElse(OesUnknownHub.create(b)); // если нет - создаю "неизвестный"
                    }

                    // подключаю объект к линии
                    port.addConection(hub.connectionByAddress(b));
                    devices.add(hub);
                });
        //endregion
    }

    private void rootOesChanged_A(StandBinaryPackage pack) {
        OesRootHub root = OesRootHub.create(pack.getTask().getPowerSystem());

        //region 1. проверка подключения к блоку управления
        Byte[] block = Arrays.stream(pack.getOesbin())
                .filter(e -> Arrays.stream(e)
                        .anyMatch(SerialElementAddresses::isControlBlock))
                .findFirst()
                .map(b -> Arrays.stream(b)
                        .filter(a -> !SerialElementAddresses.isControlBlock(a))
                        .toArray(Byte[]::new))
                .orElse(null);
        if (block == null || block.length == 0) {
            root.setError(
                    combineErrorMsg(root.getError(), Messages.SER_0));
        } else if (Arrays.stream(block)
                .noneMatch(b -> b == root.getControlPort().getAddress())) {
            root.setError(
                    combineErrorMsg(root.getError(), Messages.SER_2));
        }
        //endregion

        List<IOesHub> passingList = new ArrayList<>();

        // 2. сборка входных линий
        Arrays.stream(root.getInputs())
                        .forEach(line -> buildConnections_A(pack, passingList, line));

        logger.info("-- входные линии: {}", passingList);

        //region 3. проверки на допустимость подключений
        Arrays.stream(root.getInputs())
                .filter(e -> e.getConnections() != null && e.getConnections().length != 0)
                .forEach(e -> {
                    // должно быть не более одного объекта генерации
                    if (e.getConnections().length > 1 ||
                            (!e.getConnections()[0].getOwner().hasOwner()) ||
                            (!e.getConnections()[0].getOwner().getClass().isAssignableFrom(IGeneration.class))) {
                        e.setError(
                                combineErrorMsg(e.getError(), Messages.SER_1));
                    }
                });
        //endregion

        // 4. сборка выходных линий
        Arrays.stream(root.getOutputs())
                .forEach(line -> buildConnections_A(pack, passingList, line));

        logger.info("-- выходные линии: {}", passingList);

        //region 5. проверки на допустимость подключений
        Arrays.stream(root.getOutputs())
                .filter(e -> e.getConnections() != null && e.getConnections().length != 0)
                .forEach(e -> {
                    // могут подключаться только миниподстанции и потребители 1-й и 2-й категорий
                    if (!Arrays.stream(e.getConnections())
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
                    }

                    // одно и то же устройство не может быть подключено более одного раза
                    Set<Integer> items = new HashSet<>();
                    if (Arrays.stream(e.getConnections())
                            .filter(c -> c.getOwner().hasOwner())
                            .map(c -> c.getOwner().getAddress())
                            .anyMatch(n -> !items.add(n))) {
                        e.setError(
                                combineErrorMsg(e.getError(), Messages.SER_4));
                    }
                });
        //endregion

        /*
        // пополнить список своих устройств
        // актуально только для выходных линий
        buildDevices(devices, root.getOutputs());
        logger.info(">> buildDevices: {}", devices);

        root.setDevices(devices.size() != 0 ? devices.toArray(IOesHub[]::new) : null);
        pack.getTask().setRoot(root);
        */

        // 6. возможно, есть где-то миниподстанция...
        passingList.stream()
                .filter(IOesHub::hasOwner)
                .filter(e -> e.getOwner().getComponentType() == SupportedTypes.DISTRIBUTOR)
                .forEach(e -> Arrays.stream(pack.getTask().getRoot().getDevices())
                        // ищу в уже подключенных. "e" - новое устройство
                        .filter(a -> a.getAddress() == e.getAddress())
                        .findFirst()
                        .ifPresent(a -> Arrays.stream(a.getOutputs())
                                // перекидываю только выходы. "a" - миниподстанция в прошлом; "e" - новое устройство (миниподстанция)
                                .forEach(oldLine -> {
                                    IConnectionPort targetConn = e.connectionByAddress(oldLine.getAddress());
                                    IConnectionPort[] oldConnections = oldLine.getConnections();
                                    if (oldConnections != null) {
                                        Arrays.stream(oldConnections)
                                                .forEach(oldItem -> {
                                                    // перебираю все элементы подключений

                                                    // !!! здесь надо проверить устройство в имеющемся списке !!!

                                                    try {
                                                        if (!targetConn.addConection(
                                                                oldItem.getOwner().connectionByAddress(
                                                                        oldItem.getAddress()))) {
                                                            logger.error(Messages.FSER_2,
                                                                    oldItem.getAddress(), e.getOwner().getIdenty());
                                                        }
                                                    }
                                                    catch (NullPointerException ex) {
                                                        logger.error(Messages.FSER_3,
                                                                oldLine.getAddress(), e.getOwner().getIdenty());
                                                    }
                                                });
                                    }
                                })));

        root.setDevices(passingList.size() != 0 ? passingList.toArray(IOesHub[]::new) : null);
        pack.getTask().setRoot(root);

        // !!!
        if (root.hasError()) {
            logger.warn(root.getError());
        }
        Arrays.stream(root.getInputs())
                .filter(IConnectionPort::hasError)
                .map(IConnectionPort::getError)
                .forEach(logger::warn);
        Arrays.stream(root.getOutputs())
                .filter(IConnectionPort::hasError)
                .map(IConnectionPort::getError)
                .forEach(logger::warn);
    }

    private void rootOesChanged(StandBinaryPackage pack) {
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
            root.setError(
                    combineErrorMsg(root.getError(), Messages.SER_0));
            logger.warn(root.getError()); // !!!
        } else if (Arrays.stream(block)
                .noneMatch(b -> b == root.getControlPort().getAddress())) {
            root.setError(
                    combineErrorMsg(root.getError(), Messages.SER_2));
            logger.warn(root.getError()); // !!!
        }

        // 2. сборка входных линий
        buildConnections(pack, devices, root.getInputs());

        // проверки на допустимость подключений
        Arrays.stream(root.getInputs())
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
        buildConnections(pack, devices, root.getOutputs());

        // проверки на допустимость подключений
        Arrays.stream(root.getInputs())
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

        // пополнить список своих устройств
        // актуально только для выходных линий
        buildDevices(devices, root.getOutputs());
        logger.info(">> buildDevices: {}", devices);

        root.setDevices(devices.size() != 0 ? devices.toArray(IOesHub[]::new) : null);
        pack.getTask().setRoot(root);
    }

    private void distributorOesChanged(StandBinaryPackage pack) {
        // создаю список устройств
        List<IOesHub> devices = new ArrayList<>();

        // 1. Определяю миниподстанцию
        OesDistributorHub station = OesDistributorHub.create((EnergyDistributor) pack.getOes());

        // 2. Смотрю, куда подключен по входной линии
        EnergyDistributorSpecification pars = ((EnergyDistributor) pack.getOes()).getData();
        Byte[] block = Arrays.stream(pack.getOesbin())
                .filter(e -> Arrays.stream(e)
                        .anyMatch(b -> b == pars.getInaddr()))
                .findFirst()
                .map(b -> Arrays.stream(b)
                        .filter(a -> !(a == pars.getInaddr()))
                        .toArray(Byte[]::new))
                .orElse(null);
        if (block == null || block.length == 0) { // ко входной линии ничего не подключено
            station.setError(
                    combineErrorMsg(station.getError(), Messages.SER_5));
            logger.warn(station.getError()); // !!!
        } else {
                // что на входной линии, главная подстанция?
                if (Arrays.stream(modelingData.getTasks())
                        .noneMatch(a -> Arrays.stream(block)
                                .anyMatch(b -> a.getRoot().itIsMine(b)))) {
                    station.setError(
                            combineErrorMsg(station.getError(), Messages.SER_5));
                    logger.warn(station.getError()); // !!!
                }
            }

        // 2. Добавляю себя в список устройств
        // devices.add(station); !!!

        // 3. Собираю выходные линии
        buildConnectionsAll(pack, devices, station.getOutputs());

        // 4. проверки на допустимость подключений
        Arrays.stream(station.getOutputs())
                .forEach(e -> {
                    // могут подключаться только потребители 3-й категории
                    if (!Arrays.stream(
                                    e.getConnections() != null ? e.getConnections() : new IConnectionPort[0])
                            .allMatch(c -> {
                                if (c.getOwner().hasOwner() &&
                                        c.getOwner().getOwner().getComponentType() == SupportedTypes.CONSUMER) {
                                    return ((Consumer) c.getOwner().getOwner()).getData().getConsumertype() == SupportedConsumers.DISTRICT;
                                } else {
                                    return false;
                                }
                            })) {
                        e.setError(
                                combineErrorMsg(e.getError(), Messages.SER_6));
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

        // 5. Обновляю все устройства
        Arrays.stream(modelingData.getTasks())
                        .forEach(e -> {
                            List<IOesHub> target = new ArrayList<>();
                            logger.info(">> rebuildDeviceList: {}", e.getRoot().getDevices());
                            rebuildDeviceList(e.getRoot().getOutputs(), target, station);
                            logger.info("-- rebuildDeviceList: {}", target);
                            e.getRoot().setDevices(target.size() != 0 ? target.toArray(IOesHub[]::new) : null);
                        });
    }

    private void checkAndBuild(StandBinaryPackage pack) {
        if (pack.getTask() != null) {
            // rootOesChanged(pack);
            rootOesChanged_A(pack);
        } else if (pack.getOes().getComponentType() == SupportedTypes.DISTRIBUTOR) {
            // это или миниподстанция или потребитель 1, 2-й категорий
            // думаю, что вес имеют только миниподстанции, соответственно, потребителей скидываю...
            // distributorOesChanged(pack);
        }
    }

    @Override
    public void run() {
        logger.info("Поток обслуживания схемы модели запущен.");
        int errorCount = 0;
        boolean isMessage = false;
        while (true) {
            try {
                StandBinaryPackage pack = scheme.poll();
                if (pack == null) {
                    if (isMessage) {
                        modelingData.sendSchemeDataMessage();
                    }
                    isMessage = false;
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
                        isMessage = true;
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
                                isMessage = true;
                                pack.setOes(cmp);
                                pack.setOesbin(parsePackage(pack));
                            }
                            default -> {
                                continue;
                            }
                        }
                }

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
