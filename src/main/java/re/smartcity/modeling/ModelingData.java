package re.smartcity.modeling;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import re.smartcity.energynet.IComponentIdentification;
import re.smartcity.energynet.component.MainSubstationPowerSystem;

import java.util.Arrays;
import java.util.concurrent.Executors;

public class ModelingData {

    private final Logger logger = LoggerFactory.getLogger(ModelingData.class);
    private final Object syncObj = new Object();

    private final int discreteness = 500; // дискретность модели, не игровой

    private volatile IComponentIdentification[] allobjects = new IComponentIdentification[] { };

    private volatile GameStatuses gameStatus = GameStatuses.NONE; // !!!

    //region данные модели
    private TaskData[] tasks;

    public TaskData[] getTasks() {
        synchronized (syncObj) {
            return tasks;
        }
    }

    public void setTasks(TaskData[] tasks) {
        synchronized (syncObj) {
            this.tasks = tasks;
        }
    }

    public IComponentIdentification[] getAllobjects() {
        return allobjects;
    }

    public void setAllobjects(IComponentIdentification[] allobjects) {
        if (allobjects == null) {
            allobjects = new IComponentIdentification[] { };
        }
        this.allobjects = allobjects;
    }
    //endregion

    //region игровые данные
    public GameStatuses getGameStatus() {
        return gameStatus;
    }
    //endregion

    public void stopAll() {
        TaskData[] items = getTasks();
        if (items != null) {
            logger.info("--> остановка модели");
            for (TaskData item : items) {
                item.getService().shutdown();
                logger.info("--> остановка моделирования для {}", item.getPowerSystem().getIdenty());
            }
        }
    }

    public void putOnMonitoring(MainSubstationPowerSystem[] substations) {
        stopAll();
        TaskData[] tasks = Arrays.stream(substations).map(e -> new TaskData(Executors.newSingleThreadExecutor(), e))
                .toArray(TaskData[]::new);
        setTasks(tasks);
        for (TaskData task : tasks) {
            task.getService().execute(new SubnetMonitor(this, task));
        }
    }

}
