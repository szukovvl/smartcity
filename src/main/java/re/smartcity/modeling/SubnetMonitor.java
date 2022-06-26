package re.smartcity.modeling;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SubnetMonitor implements Runnable {

    private final Logger logger = LoggerFactory.getLogger(SubnetMonitor.class);

    private final ModelingData modelingData;

    private final TaskData taskData;

    public SubnetMonitor(ModelingData modelingData, TaskData taskData) {
        this.modelingData = modelingData;
        this.taskData = taskData;
    }

    @Override
    public void run() {
        logger.info("монитор для {} начал выполнение.", taskData.getPowerSystem().getIdenty());
        try {
            while (!taskData.getService().isShutdown() && !taskData.getService().isTerminated()) {
                Thread.sleep(800);
                System.out.println("+ " + taskData.getPowerSystem().getIdenty());
            }
        }
        catch (InterruptedException ex) {
            logger.info("работа монитора прервана.");
        }
        finally {
            logger.info("монитор для {} завершил выполнение.", taskData.getPowerSystem().getIdenty());
        }
    }
}
