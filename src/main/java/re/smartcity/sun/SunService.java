package re.smartcity.sun;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import re.smartcity.wind.*;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class SunService {

    @Autowired
    private SunStatusData statusData;
    @Autowired
    private SunControlData controlData;

    volatile private ExecutorService executorService;

    public void start() {
        if (executorService == null)
        {
            executorService = Executors.newSingleThreadExecutor();
            executorService.execute(new SunService.SunThread());
        }
    }

    public void stop() {
        if (executorService != null) {
            controlData.addCommand(new SunControlCommand(SunControlCommands.ACTIVATE, false));
            Executors.newSingleThreadExecutor().execute(() -> {
                try {
                    Thread.sleep(controlData.getWaiting() * 2);
                    executorService.shutdown();
                }
                catch (InterruptedException ignored) { }
            });
        }
    }

    // ВНИМАНИЕ: при перезапуске задач не выполняются блокировки, что может привести к запуску ложного потока
    public void restart() {
        Executors.newSingleThreadExecutor().execute(new SunService.RestartSunThread());
    }
    private class SunThread implements Runnable {

        private final Logger logger = LoggerFactory.getLogger(SunService.SunThread.class);

        public void run() {
            logger.info("поток управления солнцем запущен.");
            try {
                statusData.setStatus(WindServiceStatuses.LAUNCHED);
                while(!executorService.isShutdown() && !executorService.isTerminated()) {
                    Thread.sleep(controlData.getWaiting());

                    if (controlData.commandExists()) {
                        SunControlCommand cmd = controlData.currentCommand();
                        logger.info("выполнение команды: {}", cmd);
                        switch (cmd.command()) {
                            case ACTIVATE -> statusData.setOn(cmd.getValueAsBoolean());
                            case POWER -> statusData.setPower(cmd.getValueAsInt());
                        }
                        continue;
                    }
                }
            }
            catch (InterruptedException ex) {
                logger.warn("поток управления солнцем прерван.");
            }
            finally {
                statusData.setStatus(WindServiceStatuses.STOPPED);
                executorService = null;
                logger.info("поток управления солнцем завершил выполнение.");
            }
        }
    }

    private class RestartSunThread implements Runnable {

        public void run() {
            try {
                SunService.this.stop();
                Thread.sleep(controlData.getRestartingWait());
                SunService.this.start();
            }
            catch (InterruptedException ignored) { }
        }
    }
}
