package re.smartcity.stand;

import jssc.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import re.smartcity.common.CommonStorage;
import re.smartcity.common.data.exchange.StandConfiguration;
import re.smartcity.common.resources.Messages;
import re.smartcity.common.utils.Helpers;
import re.smartcity.wind.WindServiceStatuses;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static re.smartcity.common.utils.Helpers.byteArrayCopy;

@Service
public class StandService {

    private final Logger logger = LoggerFactory.getLogger(StandService.class);

    @Autowired
    private StandStatusData standStatus;

    @Autowired
    private CommonStorage storage;

    private final StandControlData controlData = new StandControlData();

    private volatile ExecutorService executorService;

    private final SerialCommandQueue serialCommands = new SerialCommandQueue();

    private final Object _serialLock = new Object();

    //region частные методы
    private void toOriginalState(SerialPort serialPort)
        throws SerialPortException, InterruptedException {
        // отключение осветителя
        SerialPackageBuilder.printBytes("-->", SerialPackageBuilder.setBrightnessSunSimulator(0));
        synchronized (_serialLock) {
            serialPort.writeBytes(SerialPackageBuilder.setBrightnessSunSimulator(0));
            Thread.sleep(StandControlData.DELAY_COMMAND_FLOW);
        }
        // отключение подсветки модели
        SerialPackageBuilder.printBytes("-->", SerialPackageBuilder.setHighlightLevel(0));
        synchronized (_serialLock) {
            serialPort.writeBytes(SerialPackageBuilder.setHighlightLevel(0));
            Thread.sleep(StandControlData.DELAY_COMMAND_FLOW);
        }
    }

    private void translatePacket(Byte[] packet) {

        if (packet.length < 2) {
            logger.warn(Messages.ER_12);
            return;
        }

        switch (packet[1]) {
            case SerialPackageTypes.DATA_SCHEME_CONNECTION ->
                    SerialPackageBuilder.printBytes(String.format("<-- схема %02X:", packet[0]),
                            byteArrayCopy(packet, 2, packet.length - 2));
            case SerialPackageTypes.DATA_SUPPLY_VOLTAGE -> {
                float voltage = Float.parseFloat(new String(
                        byteArrayCopy(packet, 2, packet.length - 2)));
                logger.warn(String.format(Messages.FER_2, packet[0], voltage));
            }
            case SerialPackageTypes.ILLUMINATION_DATA_SOLAR_BATTERY -> {
                int luxury = Integer.parseInt(new String(
                        byteArrayCopy(packet, 2, 4)));
                if (Arrays.stream(packet).skip(5).anyMatch(b -> b == 0)) {
                    System.out.printf("<-- СЭС %02X: %d/0\n", packet[0], luxury);
                } else {
                    int bg = Integer.parseInt(new String(
                            byteArrayCopy(packet, 6, 4)));
                    System.out.printf("<-- СЭС %02X: %d/%d\n", packet[0], luxury, bg);
                }
            }
            case SerialPackageTypes.WIND_FORCE_DATA -> {
                float windSpeed = Float.parseFloat(new String(
                        byteArrayCopy(packet, 2, 4)));
                if (Arrays.stream(packet).skip(5).anyMatch(b -> b == 0)) {
                    System.out.printf("<-- ВГ %02X: %f/0\n", packet[0], windSpeed);
                } else {
                    float calibration = Float.parseFloat(new String(
                            byteArrayCopy(packet, 6, 4)));
                    System.out.printf("<-- ВГ %02X: %f/%f\n", packet[0], windSpeed, calibration);
                }
            }
            case SerialPackageTypes.MODEL_HIGHLIGHT_DATA -> {
                int level = Integer.parseInt(new String(
                        byteArrayCopy(packet, 2, packet.length - 2)));
                System.out.printf("<-- подсветка %02X: %d\n", packet[0], level);
            }
            case SerialPackageTypes.DATA_CURRENT_CONSUMED -> logger.warn(String.format(Messages.FER_3, packet[0]));
            case SerialPackageTypes.INTERNAL_BUFFER_OVERFLOW -> logger.warn(String.format(Messages.FER_4, packet[0]));
            default -> logger.warn(String.format(Messages.FER_5, packet[1], packet[0]));
        }
    }
    //endregion

    //region общедоступные методы
    public void start() {
        logger.info("запуск сервиса управления стендом");
        if (executorService == null)
        {
            executorService = Executors.newSingleThreadExecutor();
            executorService.execute(new StandThread());
        } else {
            logger.info("сервис управления стендом уже запущен");
        }
    }

    public void stop() {
        logger.info("останов сервиса управления стендом");
        if (executorService != null) {
            executorService.shutdown();
        } else {
            logger.info("сервис управления стендом не запущена");
        }
    }

    public void restart() {
        logger.info("перезапуск сервиса управления стендом");
        Executors.newSingleThreadExecutor().execute(new RestartThread());
    }

    public Mono<StandControlData> loadConfiguration() {
        return storage.getAndCreate(StandConfiguration.key, StandConfiguration.class)
                .map(data -> {
                    controlData.apply(data.getData());
                    return controlData;
                });
    }

    public StandControlData getControlData() {
        return controlData;
    }

    public Mono<Integer> setControlData(StandControlData src) {
        controlData.apply(src);

        return storage.putData(StandConfiguration.key, src, StandConfiguration.class);
    }

    public void pushSerialCommand(SerialCommand command) {
        serialCommands.pushCommand(command);
    }
    //endregion

    //region внутренние классы
    private class StandThread implements Runnable {

        private final Logger logger = LoggerFactory.getLogger(StandThread.class);

        public void run() {
            // подготовка
            if (controlData.getPort() == null || "".equals(controlData.getPort())) {
                // порт не задан
                // выполняею поиск возможного порта подключения
                String[] portNames = SerialPortList.getPortNames();
                if (portNames.length != 0) {
                    logger.info("Обнаружены доступные порты:");
                    Arrays.stream(portNames).forEach(port -> logger.info("\tпорт: {}", port));
                    // проверяю имена для линксовых систем
                    String[] linuxPorts = Arrays.stream(portNames).filter(e -> e.contains("USB")).toArray(String[]::new);
                    if (linuxPorts.length == 1) {
                        controlData.setPort(linuxPorts[0]);
                        setControlData(controlData)
                                .subscribe();
                    } else {
                        String[] windowsPorts = Arrays.stream(portNames).filter(e -> e.contains("COM")).toArray(String[]::new);
                        if (windowsPorts.length == 1) {
                            controlData.setPort(windowsPorts[0]);
                            setControlData(controlData)
                                    .subscribe();
                        }
                    }
                } else {
                    executorService = null;
                    standStatus.setErrorMsg(Messages.ER_11);
                    logger.warn(Messages.ER_11);
                    return;
                }
            }

            if (controlData.getPort() == null || "".equals(controlData.getPort())) {
                executorService = null;
                standStatus.setErrorMsg(Messages.ER_10);
                logger.warn(Messages.ER_10);
                return;
            }

            // конфигурация порта 1200-8-N-1

            logger.info("подключаемый порт к блоку управления {}", controlData.getPort());

            SerialPort serialPort = new SerialPort(controlData.getPort());
            try {
                serialPort.openPort();
                serialPort.setParams(
                        SerialPort.BAUDRATE_1200,
                        SerialPort.DATABITS_8,
                        SerialPort.STOPBITS_1,
                        SerialPort.PARITY_NONE);

                serialPort.setEventsMask(SerialPort.MASK_RXCHAR);
                serialPort.addEventListener(new StandPortEventListener());
            }
            catch (SerialPortException ex) {
                executorService = null;
                logger.error(ex.getMessage());
                standStatus.setErrorMsg(ex.getMessage());
                if (serialPort.isOpened()) {
                    try {
                        serialPort.closePort();
                    }
                    catch (SerialPortException ignored) { }
                }
                return;
            }

            try {
                // привожу все в исходное состояние
                toOriginalState(serialPort);
                // опрос схемы подключения
            }
            catch (SerialPortException | InterruptedException ex) {
                executorService = null;
                logger.error(ex.getMessage());
                standStatus.setErrorMsg(ex.getMessage());
                if (serialPort.isOpened()) {
                    try {
                        serialPort.closePort();
                    }
                    catch (SerialPortException ignored) { }
                }
                return;
            }

            // ...
            try {
                standStatus.setErrorMsg(null);
                logger.info("поток управления стендом запущен.");
                standStatus.setStatus(WindServiceStatuses.LAUNCHED);
                serialCommands.clear();
                pushSerialCommand(new SerialCommand(SerialPackageTypes.REQUEST_SCHEME_CONNECTION_ELEMENTS));
                while(!executorService.isShutdown() && !executorService.isTerminated()) {
                    if (serialCommands.empty()) {
                        Thread.sleep(StandControlData.DELAY_WHEN_EMPTY);
                    } else {
                        SerialCommand cmd = serialCommands.poll();
                        if (cmd != null) {
                            SerialPackageBuilder.printBytes("-->", SerialPackageBuilder.createPackage(cmd));
                            synchronized (_serialLock) {
                                serialPort.writeBytes(SerialPackageBuilder.createPackage(cmd));
                                Thread.sleep(StandControlData.DELAY_COMMAND_FLOW);
                            }
                        }
                    }
                }
            }
            catch (SerialPortException ex) {
                logger.error(ex.getMessage());
                standStatus.setErrorMsg(ex.getMessage());
            }
            catch (InterruptedException ex) {
                logger.info("поток управления стендом прерван.");
            }
            finally {
                executorService = null;
                // привожу все в исходное состояние
                try {
                    toOriginalState(serialPort);
                }
                catch (SerialPortException | InterruptedException ignored) { }
                try {
                    serialPort.closePort();
                }
                catch (SerialPortException ignored) { }
                logger.info("поток управления стендом завершил выполнение.");
                standStatus.setStatus(WindServiceStatuses.STOPPED);
            }
        }
    }

    private class StandPortEventListener implements SerialPortEventListener {

        private final Logger logger = LoggerFactory.getLogger(StandPortEventListener.class);

        private final ArrayList<Byte> pack = new ArrayList<>();
        private boolean startOk = false;
        private byte[] buffer;

        @Override
        public void serialEvent(SerialPortEvent event) {
            if (event.isRXCHAR() && event.getEventValue() > 0) {

                try {
                    synchronized (_serialLock) {
                        buffer = event.getPort().readBytes(event.getEventValue());
                        Thread.sleep(StandControlData.DELAY_COMMAND_FLOW);
                    }
                }
                catch (SerialPortException ex) {
                    logger.error(ex.getMessage());
                }
                catch (InterruptedException ex) { // предполагаю останов сервиса
                    startOk = false;
                    pack.clear();
                    return;
                }

                for (byte b : buffer) {
                    if (startOk) { // ищу завершение пакета
                        if (b != SerialServiceSymbols.PACKAGE_END) {
                            pack.add(b);
                            if (b == SerialServiceSymbols.PACKAGE_START) {
                                logger.error("неожиданное начало пакета");
                                startOk = true;
                                pack.clear();
                            }
                        } else {
                            Byte[] packet = pack.toArray(Byte[]::new);
                            pack.clear();
                            startOk = false;
                            Executors.newSingleThreadExecutor().execute(() -> translatePacket(packet));
                        }
                    } else { // ищу начало пакета
                        if (b == SerialServiceSymbols.PACKAGE_START) {
                            startOk = true;
                            pack.clear();
                        }
                    }
                }
            }
        }
    }

    private class RestartThread implements Runnable {

        private final Logger logger = LoggerFactory.getLogger(RestartThread.class);

        public void run() {
            logger.info("перезапуск потока управления стендом.");
            try {
                StandService.this.stop();
                Thread.sleep(controlData.getRestartingWait());
                StandService.this.start();
            }
            catch (InterruptedException ex) {
                logger.info("перезапуск потока управления стендом - прерван.");
            }
        }
    }
    //endregion
}
