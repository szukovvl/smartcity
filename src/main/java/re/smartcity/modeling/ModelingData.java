package re.smartcity.modeling;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import re.smartcity.energynet.IComponentIdentification;
import re.smartcity.energynet.component.EnergyDistributor;
import re.smartcity.energynet.component.MainSubstationPowerSystem;
import re.smartcity.modeling.data.GamerScenesData;

import java.time.LocalTime;
import java.util.concurrent.Executors;

public class ModelingData {

    private final Logger logger = LoggerFactory.getLogger(ModelingData.class);
    private final Object syncObj = new Object();

    private final int discreteness = 500; // дискретность модели, не игровой

    private volatile IComponentIdentification[] allobjects = new IComponentIdentification[] { };

    private volatile GameStatuses gameStatus = GameStatuses.NONE; //
    private volatile LocalTime gamingDay = LocalTime.of(1, 0);

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

    public void setGameStatus(GameStatuses gameStatus) {
        this.gameStatus = gameStatus;
    }

    public LocalTime getGamingDay() {
        return gamingDay;
    }

    public void setGamingDay(LocalTime gamingDay) {
        this.gamingDay = gamingDay;
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

    public void putOnMonitoring(MainSubstationPowerSystem[] mainstations, EnergyDistributor[] substations) {
        stopAll();
        TaskData[] tasks = new TaskData[mainstations.length];
        for (int i = 0; i < tasks.length; i++) {
            tasks[i] = new TaskData(Executors.newSingleThreadExecutor(), mainstations[i], new GamerScenesData(substations[i]));
        }
        setTasks(tasks);
        for (TaskData task : tasks) {
            task.getService().execute(new SubnetMonitor(this, task));
        }
    }

    public void cancelScenes() {
        setGameStatus(GameStatuses.NONE);
        // !!!
    }
}
