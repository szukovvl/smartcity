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
                    boolean toList = false;
                    if (hub == null) { // устройства нет
                        toList = true;
                                // ищу сам компонент
                        hub = Arrays.stream(modelingData.getAllobjects())
                                .filter(a -> a.itIsMine(b))
                                .findFirst()
                                .map(OesRootHub::createOther)
                                .orElse(OesUnknownHub.create(b)); // если нет - создаю "неизвестный"
                    }

                    // подключаю объект к линии
                    port.addConection(hub.connectionByAddress(b));
                    if (toList) {
                        devices.add(hub);
                    }
                });
        //endregion
    }

    private boolean isGenerationOes(IComponentIdentification oes) {
        return oes.getComponentType() == SupportedTypes.GENERATOR ||
                oes.getComponentType() == SupportedTypes.GREEGENERATOR ||
                oes.getComponentType() == SupportedTypes.STORAGE;
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

        root.setConnected(root.getError() == null); // если есть ошибки - подключение к блоку управления отсутствует
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
                            (!isGenerationOes(e.getConnections()[0].getOwner().getOwner()))) {
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
                                        case DISTRIBUTOR -> {
                                            return c.getAddress() == c.getOwner().getInputs()[0].getAddress();
                                        }
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

        //region 6. возможно, есть где-то миниподстанция...
        List<IOesHub> newDev = new ArrayList<>();
        passingList.stream()
        // Arrays.stream(passingList.toArray(IOesHub[]::new)) // !!! (???)
                .filter(IOesHub::hasOwner)
                .filter(e -> e.getOwner().getComponentType() == SupportedTypes.DISTRIBUTOR)
                .forEach(e -> Arrays.stream(
                            pack.getTask().getRoot().getDevices() != null
                                    ? pack.getTask().getRoot().getDevices()
                                    : new IOesHub[0]
                        )
                        // ищу в уже подключенных. "e" - новое устройство
                        .filter(a -> a.getAddress() == e.getAddress())
                        .findFirst()
                        .ifPresent(a -> Arrays.stream(a.getOutputs() != null
                                        ? a.getOutputs()
                                        : new IConnectionPort[0])
                                // перекидываю только выходы. "a" - миниподстанция в прошлом; "e" - новое устройство (миниподстанция)
                                .forEach(oldLine -> {
                                    IConnectionPort[] oldConnections = oldLine.getConnections();
                                    if (oldConnections != null && oldConnections.length != 0) {
                                        IConnectionPort targetConn = e.connectionByAddress(oldLine.getAddress());
                                        if (targetConn != null) { // !!!
                                            Arrays.stream(oldConnections)
                                                    .forEach(oldItem -> {
                                                        // перебираю все элементы подключений
                                                        IOesHub hub = passingList.stream()
                                                                .filter(h -> h.getAddress() == oldItem.getOwner().getAddress())
                                                                .findFirst()
                                                                .orElse(newDev.stream()
                                                                        .filter(h -> h.getAddress() == oldItem.getOwner().getAddress())
                                                                        .findFirst()
                                                                        .orElse(null));
                                                        if (hub == null) { // !!! новое устройство (?)
                                                            hub = oldItem.getOwner();
                                                            newDev.add(hub);
                                                        }

                                                        try {
                                                            if (!targetConn.addConection(
                                                                    hub.connectionByAddress(
                                                                            oldItem.getAddress()))) {
                                                                logger.error(Messages.FSER_2,
                                                                        oldItem.getAddress(), e.getOwner().getIdenty());
                                                            }
                                                        } catch (NullPointerException ex) {
                                                            logger.error(Messages.FSER_3,
                                                                    oldLine.getAddress(), e.getOwner().getIdenty());
                                                        }

                                                        // куда подключена...
                                                        if (Arrays.stream(root.getOutputs())
                                                                .filter(c -> c.getConnections() != null)
                                                                .flatMap(c -> Stream.of(c.getConnections()))
                                                                .noneMatch(c -> Arrays.stream(e.getInputs())
                                                                        .anyMatch(d -> d.getAddress() == c.getAddress()))) {
                                                            e.setError(
                                                                    combineErrorMsg(e.getError(), Messages.SER_7));
                                                        }

                                                        // что подключено ...
                                                        Arrays.stream(e.getOutputs() != null
                                                                        ? e.getOutputs()
                                                                        : new IConnectionPort[0])
                                                                .filter(d -> d.getConnections() != null)
                                                                .forEach(d -> Arrays.stream(d.getConnections())
                                                                        .forEach(b -> {
                                                                            if (b.getOwner().hasOwner()) {
                                                                                if (b.getOwner().getOwner().getComponentType() == SupportedTypes.CONSUMER) {
                                                                                    ConsumerSpecification data = ((Consumer) b.getOwner().getOwner()).getData();
                                                                                    if (data.getConsumertype() != SupportedConsumers.DISTRICT) {
                                                                                        d.setError(
                                                                                                combineErrorMsg(d.getError(), Messages.SER_6));
                                                                                    }
                                                                                } else {
                                                                                    d.setError(
                                                                                            combineErrorMsg(d.getError(), Messages.SER_6));
                                                                                }
                                                                            } else {
                                                                                d.setError(
                                                                                        combineErrorMsg(d.getError(), Messages.SER_6));
                                                                            }
                                                                        }));
                                                    });
                                        }
                                    }
                                })));
        passingList.addAll(newDev);
        //endregion

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

    private void distributorOesChanged_A(StandBinaryPackage pack) {
        // подключаемая станция
        OesDistributorHub station = OesDistributorHub.create((EnergyDistributor) pack.getOes());

        //region 1. определяю, куда подключена подстанция...
        Byte[] block = Arrays.stream(pack.getOesbin())
                .filter(e -> Arrays.stream(e)
                        .anyMatch(b -> b == station.getInputs()[0].getAddress())) // миниподстанция имеет только один порт подключения
                .findFirst()
                .map(b -> Arrays.stream(b)
                        .filter(a -> !(a == station.getInputs()[0].getAddress()))
                        .toArray(Byte[]::new))
                .orElse(new Byte[0]);

        // получил список устройств, которые есть на входной линии миниподстанции
        // выделяю главную подстанцию
        TaskData task = null;
        for (TaskData item : modelingData.getTasks()) {
            if (Arrays.stream(block)
                    .anyMatch(b -> Arrays.stream(item.getRoot().getOutputs())
                            .anyMatch(a -> a.getAddress() == b))) {
                task = item;
                break;
            }
        }
        if (task == null) {
            // подключена куда-то в другое место...
            // ищу в списках устройств на наличие данной подстанции
            IOesHub hub = Arrays.stream(modelingData.getTasks())
                    .map(TaskData::getRoot)
                    .flatMap(e -> Stream.of(e.getDevices()))
                    .filter(e -> e.getAddress() == station.getAddress())
                    .findFirst()
                    .orElse(null);
            if (hub != null) {
                // нашел где-то свою подстанцию и устанавливаю для нее ошибку
                hub.setError(combineErrorMsg(
                        hub.getError(), Messages.SER_7));
                logger.warn(hub.getError()); // !!!
                // больше ничего не делаю и прекращаю обработку...
            } else {
                // непонятно, где болтается данная миниподстанция...
                // запрашиваю схему повторно, на всякий случай.
                // !!! сделю позже
                logger.warn("(!-1) запросить схему повторно для {} (#{})",
                        station.getOwner().getIdenty(), station.getAddress()); // !!!
            }
            return;
        }

        // подстанцию определил, выбираю порт подключения...
        // !!! не проверяю объединение выходов подстанций (подстанции)
        // выделяю себя из списка устройств, прикрепленного к данной подстанции...
        IOesHub oldStation = Arrays.stream(task.getRoot().getDevices() != null
                        ? task.getRoot().getDevices()
                        : new IOesHub[0])
                .filter(e -> e.getAddress() == station.getAddress())
                .findFirst()
                .orElse(null);
        IConnectionPort mainPort = null;
        for (IConnectionPort output : task.getRoot().getOutputs()) {
            if (output.getConnections() != null) {
                if (Arrays.stream(output.getConnections())
                        .anyMatch(conn -> conn.getAddress() == station.getInputs()[0].getAddress())) {
                    mainPort = output;
                    break;
                }
            }
        }
        if (mainPort == null) {
            // к главной станции не подключена...
            if (oldStation != null) {
                oldStation.setError(combineErrorMsg(
                        oldStation.getError(), Messages.SER_7));
                logger.warn(oldStation.getError()); // !!!
            } else {
                // запросить схему повторно?
                // !!! сделю позже
                logger.warn("(!-2) запросить схему повторно для {} (#{})",
                        station.getOwner().getIdenty(), station.getAddress()); // !!!
            }
            return;
        }
        //endregion

        // вроде подключена нормально...
        //region 2. корректирую существующие подключения (список не пересобираю заново)
        // удаляю подключение из существующих
        mainPort.setConnections(Arrays.stream(mainPort.getConnections())
                .filter(conn -> conn.getAddress() != station.getInputs()[0].getAddress())
                .toArray(IConnectionPort[]::new));

        // удаляю старое устройство из списка
        logger.info("(1): {}", task.getRoot().getDevices() != null ? task.getRoot().getDevices().length : 0);

        // список без станции
        List<IOesHub> passingList = new ArrayList<>(Arrays.stream(
                        task.getRoot().getDevices() != null
                                ? task.getRoot().getDevices()
                                : new IOesHub[0])
                .filter(dev -> dev.getAddress() != station.getAddress())
                .toList());
        passingList.stream()
                .filter(item -> item.hasOwner())
                .forEach(item -> {
                    logger.info("** {}", item.getOwner().getIdenty());
                });
        IConnectionPort[] usedPorts = Stream.concat(
                Stream.concat(Stream.of(task.getRoot().getOutputs()),
                                Stream.of(task.getRoot().getInputs()))
                        .filter(item -> item.getConnections() != null)
                        .flatMap(item -> Stream.of(item.getConnections())),
                        passingList.stream()
                                .filter(item -> item.supportOutputs())
                                .flatMap(item -> Stream.of(item.getOutputs()))
                                .filter(item -> item.getConnections() != null)
                                .flatMap(item -> Stream.of(item.getConnections()))
                )
                .toArray(IConnectionPort[]::new);

        Arrays.stream(usedPorts)
                .forEach(item -> {
                    logger.info("+ подключение {} - {} - {}",
                            item.getAddress(),
                            item.getOwner().getAddress(),
                            item.getOwner().hasOwner() ? item.getOwner().getOwner().getIdenty() : '#');
                });

        passingList = new ArrayList<>(passingList.stream()
                //.filter(item -> item.supportInputs())
                .filter(item -> Arrays.stream(usedPorts)
                        .anyMatch(port -> item.itIsMine(port.getAddress())))
                .toList());

        logger.info("(3): {}", passingList.size());

        // создаю подключение и добавляю себя в список
        passingList.add(station);
        mainPort.addConection(station.getInputs()[0]);
        //endregion

        // 3. сборка выходных линий
        List<IOesHub> finalPassingList = passingList;
        Arrays.stream(station.getOutputs())
                .forEach(line -> buildConnections_A(pack, finalPassingList, line));

        logger.info("-- {} выходные линии: {}", station.getOwner().getIdenty(), passingList);

        // 4. проверяю допустимость подключений...
        Arrays.stream(station.getOutputs())
                .filter(e -> e.getConnections() != null && e.getConnections().length != 0)
                .forEach(e -> {
                    // могут подключаться только потребители 3-й категории
                    if (!Arrays.stream(e.getConnections())
                            .allMatch(c -> {
                                if (c.getOwner().hasOwner() &&
                                        c.getOwner().getOwner().getComponentType() == SupportedTypes.CONSUMER) {
                                    ConsumerSpecification pars = ((Consumer) c.getOwner().getOwner()).getData();
                                    return pars.getConsumertype() == SupportedConsumers.DISTRICT;
                                } else {
                                    return false;
                                }
                            })) {
                        e.setError(
                                combineErrorMsg(e.getError(), Messages.SER_6));
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

        // 5. сохраняю
        task.getRoot().setDevices(passingList.size() != 0 ? passingList.toArray(IOesHub[]::new) : null);

        // !!!
        Arrays.stream(station.getOutputs())
                .filter(IConnectionPort::hasError)
                .map(IConnectionPort::getError)
                .forEach(logger::warn);
    }

    private void checkAndBuild(StandBinaryPackage pack) {
        if (pack.getTask() != null) {
            rootOesChanged_A(pack);
        } else if (pack.getOes().getComponentType() == SupportedTypes.DISTRIBUTOR) {
            // это или миниподстанция или потребитель 1, 2-й категорий
            // думаю, что вес имеют только миниподстанции, соответственно, потребителей скидываю...
            distributorOesChanged_A(pack);
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
                    case CONTROL_BLOCK -> {
                        isMessage = true;
                        Arrays.stream(modelingData.getTasks())
                                .map(TaskData::getRoot)
                                .forEach(root -> {
                                    if (Arrays.stream(pack.getData())
                                            .noneMatch(root::itIsMine)) {
                                        root.setConnected(false);
                                    }
                                });
                        continue;
                    }
                    case MAIN_SUBSTATION_1, MAIN_SUBSTATION_2 -> {
                        isMessage = true;
                        pack.setTask(Arrays.stream(modelingData.getTasks())
                                .filter(e -> e.getPowerSystem().getDevaddr() == pack.getDevaddr())
                                .findFirst()
                                .get());
                        pack.setOesbin(parsePackage(pack));
                    }
                    default -> {
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
                ex.printStackTrace();
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
