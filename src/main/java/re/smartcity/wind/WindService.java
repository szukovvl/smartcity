package re.smartcity.wind;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class WindService {

    private final Logger logger = LoggerFactory.getLogger(WindService.class);

    @Autowired
    private WindStatusData statusData;
    @Autowired
    private WindControlData controlData;

    volatile private ExecutorService executorService;

    public void start() {
        logger.info("запуск сервиса управления ветром");
        if (executorService == null)
        {
            executorService = Executors.newSingleThreadExecutor();
            executorService.execute(new WindThread());
        } else {
            logger.info("сервис управления ветром уже запущен");
        }
    }

    public void stop() {
        logger.info("останов сервиса управления ветром");
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
            logger.info("сервис управления ветром не запущена");
        }
    }

    public void restart() {
        logger.info("перезапск сервиса управления ветром");
        Executors.newSingleThreadExecutor().execute(new RestartWindThread());
    }
    private class WindThread implements Runnable {

        private final Logger logger = LoggerFactory.getLogger(WindService.WindThread.class);

        public void run() {
            logger.info("поток управления ветром запущен.");
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
                logger.info("поток управления ветром прерван.");
            }
            finally {
                statusData.setStatus(WindServiceStatuses.STOPPED);
                executorService = null;
                logger.info("поток управления ветром завершил выполнение.");
            }
        }
    }

    private class RestartWindThread implements Runnable {

        private final Logger logger = LoggerFactory.getLogger(WindService.RestartWindThread.class);

        public void run() {
            logger.info("перезапуск потока управления ветром.");
            try {
                WindService.this.stop();
                Thread.sleep(controlData.getRestartingWait());
                WindService.this.start();
            }
            catch (InterruptedException ex) {
                logger.info("перезапуск потока управления ветром - прерван.");
            }
        }
    }
}
