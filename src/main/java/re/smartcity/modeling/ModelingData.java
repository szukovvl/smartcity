package re.smartcity.modeling;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import re.smartcity.config.sockets.GameSocketHandler;
import re.smartcity.energynet.IComponentIdentification;
import re.smartcity.energynet.component.EnergyDistributor;
import re.smartcity.energynet.component.MainSubstationPowerSystem;
import re.smartcity.modeling.data.GamerScenesData;
import re.smartcity.modeling.data.StandBinaryPackage;
import re.smartcity.stand.SerialPackageBuilder;

import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static re.smartcity.common.utils.Helpers.byteArrayCopy;

public class ModelingData {

    private final Logger logger = LoggerFactory.getLogger(ModelingData.class);
    private final Object syncObj = new Object();

    private volatile IComponentIdentification[] allobjects = new IComponentIdentification[] { };

    private volatile GameStatuses gameStatus = GameStatuses.NONE; //
    private volatile LocalTime gamingDay = LocalTime.of(1, 0);

    private final ConcurrentLinkedQueue<StandBinaryPackage> standSchemes = new ConcurrentLinkedQueue<>();
    private final Object _syncSchemeData = new Object();
    private final Map<Integer, Double> green_generation = new HashMap<>();

    private GameSocketHandler messenger;

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
                item.stopGame();
            }
        }
    }

    public void putOnMonitoring(MainSubstationPowerSystem[] mainstations, EnergyDistributor[] substations) {
        stopAll();
        TaskData[] tasks = new TaskData[mainstations.length];
        for (int i = 0; i < tasks.length; i++) {
            tasks[i] = new TaskData(
                    mainstations[i],
                    new GamerScenesData(substations[i]));
        }
        setTasks(tasks);

        // !!!
        Executors.newSingleThreadScheduledExecutor().schedule(
                new OesSchemeMonitor(this, _syncSchemeData, standSchemes),
                3,
                TimeUnit.SECONDS
        );
    }

    public void cancelScenes() {
        setGameStatus(GameStatuses.NONE);
        // !!!
    }

    public void standSchemeChanged(byte devaddr, Byte[] data) {
        SerialPackageBuilder.printBytes(
                String.format("<-- схема %02X:", devaddr),
                byteArrayCopy(data)
        );
        standSchemes.offer(new StandBinaryPackage(devaddr, data));
        synchronized (_syncSchemeData) {
            _syncSchemeData.notifyAll();
        }
    }

    public void setMessenger(GameSocketHandler messenger) {
        this.messenger = messenger;
    }

    public void sendSchemeDataMessage() {
        if (this.messenger != null) {
            messenger.sendSchemeDataMessage(null);
        }
    }

    public synchronized void putGreenGeneration(int key, double val) {
        this.green_generation.put(key, val);
    }

    public synchronized double getGreenGeneration(int key) {
        return this.green_generation.getOrDefault(key, 0.0);
    }
}
