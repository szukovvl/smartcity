package re.smartcity.stand;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import re.smartcity.modeling.ModelingData;
import re.smartcity.wind.WindServiceStatuses;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class StandService {

    private final Logger logger = LoggerFactory.getLogger(StandService.class);

    @Autowired
    private StandControlData controlData;

    @Autowired
    private ModelingData modelingData;

    @Autowired
    private StandStatusData standStatus;

    private volatile ExecutorService executorService;

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
            Executors.newSingleThreadExecutor().execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        modelingData.translate();
                        Thread.sleep(controlData.getWaiting() * 2);
                        executorService.shutdown();
                    }
                    catch (InterruptedException ex) { }
                }
            });
        } else {
            logger.info("сервис управления стендом не запущена");
        }
    }

    public void restart() {
        logger.info("перезапуск сервиса управления стендом");
        Executors.newSingleThreadExecutor().execute(new RestartThread());
    }

    private class StandThread implements Runnable {

        private final Logger logger = LoggerFactory.getLogger(StandThread.class);

        public void run() {
            logger.info("поток управления стендом запущен.");
            standStatus.setStatus(WindServiceStatuses.LAUNCHED);
            try {
                while(!executorService.isShutdown() && !executorService.isTerminated()) {
                    Thread.sleep(controlData.getWaiting());

                }
            }
            catch (InterruptedException ex) {
                logger.info("поток управления стендом прерван.");
            }
            finally {
                logger.info("поток управления стендом завершил выполнение.");
                standStatus.setStatus(WindServiceStatuses.STOPPED);
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
}
