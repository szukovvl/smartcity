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

    private volatile int discreteness = 500; // дискретность модели, не игровой

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

    public void translate() {

    }

    public void stopAll() {
        TaskData[] items = getTasks();
        if (items != null) {
            logger.info("--> остановка модели");
            for (TaskData item : items) {
                item.getService().shutdown();
                logger.info("--> остановка моделирования для %s", item.getPowerSystem().getIdenty());
            }
        }
    }

    public void putOnMonitoring(MainSubstationPowerSystem[] substations) {
        stopAll();
        TaskData[] tasks = Arrays.stream(substations).map(e -> {
                    return new TaskData(Executors.newSingleThreadExecutor(), e);
                })
                .toArray(TaskData[]::new);
        setTasks(tasks);
        for (TaskData task : tasks) {
            task.getService().execute(new SubnetMonitor(this, task));
        }
    }

}