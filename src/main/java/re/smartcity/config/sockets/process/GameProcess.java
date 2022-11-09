package re.smartcity.config.sockets.process;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import re.smartcity.config.sockets.GameSocketHandler;
import re.smartcity.modeling.ModelingData;
import re.smartcity.modeling.TaskData;
import re.smartcity.modeling.scheme.IConnectionPort;

import java.util.HashMap;
import java.util.Map;

public class GameProcess implements Runnable {

    public final static int SECONDS_OF_DAY = 86400; // количество секунд в сутках
    public final static int MODEL_DISCRET = 250; // время дискретизации в мс

    private final Logger logger = LoggerFactory.getLogger(GameProcess.class);

    private final TaskData task;
    private final GameSocketHandler messenger;
    private final ModelingData modelingData;

    public GameProcess(TaskData task, GameSocketHandler messenger, ModelingData modelingData) {
        this.task = task;
        this.messenger = messenger;
        this.modelingData = modelingData;
    }

    private double getSecondInMillis() {
        return (((double) modelingData.getGamingDay().toSecondOfDay()) / ((double) SECONDS_OF_DAY)) * 1000;
    }

    @Override
    public void run() {
        logger.info("Игровой сценарий для {} запущен", task.getPowerSystem().getIdenty());
        double secInMillis = getSecondInMillis(); // реальных секунд в мс
        long gameStep = Math.round(MODEL_DISCRET / secInMillis); // дискретизация - шаг игры в секундах
        long delay = Math.round((gameStep * secInMillis) / 10.0) * 10L; // дискретизация - в мс

        Map<Integer, IConnectionPort> ports = new HashMap<>();

        int totalSec = 0;
        GameDataset dataset = new GameDataset(task.getPowerSystem().getDevaddr());
        dataset.setCumulative_total(GameValues.builder()
                .credit(task.getGameBlock().getCredit_total())
                .build());

        messenger.gameTracertMessage(null, dataset);

        try {
            while(totalSec < SECONDS_OF_DAY) {
                Thread.sleep(delay);
                totalSec += gameStep;
                dataset.setSeconds(totalSec);
                messenger.gameTracertMessage(null, dataset);

            }
        }
        catch (InterruptedException ignored) {
            logger.warn("Игровой сценарий для {} прерван", task.getPowerSystem().getIdenty());
        }

        logger.info("Игровой сценарий для {} завершен", task.getPowerSystem().getIdenty());
    }
}
