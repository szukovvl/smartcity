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

    private final Logger logger = LoggerFactory.getLogger(SunService.class);

    @Autowired
    private SunStatusData statusData;
    @Autowired
    private SunControlData controlData;

    volatile private ExecutorService executorService;

    public void start() {
        logger.info("запуск сервиса управления солнцем");
        if (executorService == null)
        {
            executorService = Executors.newSingleThreadExecutor();
            executorService.execute(new SunService.SunThread());
        } else {
            logger.info("сервис управления солнцем уже запущен");
        }
    }

    public void stop() {
        logger.info("останов сервиса управления солнцем");
        if (executorService != null) {
            controlData.addCommand(new WindControlCommand(WindControlCommands.ACTIVATE, false));
            Executors.newSingleThreadExecutor().execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(controlData.getWaiting() * 2);
                        executorService.shutdown();
                    }
                    catch (InterruptedException ex) { }
                }
            });
        } else {
            logger.info("сервис управления солнцем не запущена");
        }
    }

    // ВНИМАНИЕ: при перезапуске задач не выполняются блокировки, что может привести к запуску ложного потока
    public void restart() {
        logger.info("перезапуск сервиса управления солнцем");
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
                        WindControlCommand cmd = controlData.currentCommand();
                        logger.info("выполнение команды: {}", cmd);
                        switch (cmd.getCommand()) {
                            case ACTIVATE -> { statusData.setOn(cmd.getValueAsBoolean()); }
                            case POWER -> { statusData.setPower(cmd.getValueAsInt()); }
                        }
                        continue;
                    }
                }
            }
            catch (InterruptedException ex) {
                logger.info("поток управления солнцем прерван.");
            }
            finally {
                statusData.setStatus(WindServiceStatuses.STOPPED);
                executorService = null;
                logger.info("поток управления солнцем завершил выполнение.");
            }
        }
    }

    private class RestartSunThread implements Runnable {

        private final Logger logger = LoggerFactory.getLogger(SunService.RestartSunThread.class);

        public void run() {
            logger.info("перезапуск потока управления солнцем.");
            try {
                SunService.this.stop();
                Thread.sleep(controlData.getRestartingWait());
                SunService.this.start();
            }
            catch (InterruptedException ex) {
                logger.info("перезапуск потока управления солнцем - прерван.");
            }
        }
    }
}
