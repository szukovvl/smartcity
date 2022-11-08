package re.smartcity.config.sockets.process;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import re.smartcity.config.sockets.GameSocketHandler;
import re.smartcity.modeling.ModelingData;
import re.smartcity.modeling.TaskData;

import java.sql.Time;
import java.time.LocalTime;

public class GameProcess implements Runnable {

    public final static int SECONDS_OF_DAY = 86400;
    public final static int MODEL_DISCRET = 250;

    private final Logger logger = LoggerFactory.getLogger(GameProcess.class);

    private final TaskData task;
    private final GameSocketHandler messenger;
    private final ModelingData modelingData;

    public GameProcess(TaskData task, GameSocketHandler messenger, ModelingData modelingData) {
        this.task = task;
        this.messenger = messenger;
        this.modelingData = modelingData;
    }

    @Override
    public void run() {
        logger.info("++++++++++++++++");
        int seconds = modelingData.getGamingDay().toSecondOfDay();
        double compression = ((double) seconds) / ((double) SECONDS_OF_DAY);
        double secInMillis = compression * 1000;
        long gameStep = Math.round(MODEL_DISCRET / secInMillis);
        long delay = Math.round((gameStep * secInMillis) / 10.0) * 10L;

        long totalSec = 0;
        LocalTime t = LocalTime.of(0, 0, 0);

        try {
            logger.info(" старт: {}", t);
            while(totalSec <= SECONDS_OF_DAY) {
                Thread.sleep(delay);
                t = t.plusSeconds(gameStep);
                totalSec += gameStep;
                logger.info(" - : {}", t);
                //messenger.gemeTracertMessage(null);

            }
        }
        catch (InterruptedException ignored) { }

        logger.info(" стоп: {}", t);

        logger.info("--------------------");


        /*logger.info("монитор для {} начал выполнение.", taskData.getPowerSystem().getIdenty());
        try {
            while (!taskData.getService().isShutdown() && !taskData.getService().isTerminated()) {
                Thread.sleep(300);
            }
        }
        catch (InterruptedException ex) {
            logger.info("работа монитора прервана.");
        }
        finally {
            logger.info("монитор для {} завершил выполнение.", taskData.getPowerSystem().getIdenty());
        }*/
    }
}
