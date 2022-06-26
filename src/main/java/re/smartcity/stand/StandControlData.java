package re.smartcity.stand;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import re.smartcity.wind.WindControlCommand;
import re.smartcity.wind.WindControlData;

import java.util.concurrent.ConcurrentLinkedQueue;

public class StandControlData {

    private final Logger logger = LoggerFactory.getLogger(StandControlData.class);
    volatile private Integer waiting = 500; // ожидание между опросом ПЛК
    volatile private Integer restartingWait = 3000; // ожидание при перезапуске, после останова сервиса

    public Integer getWaiting() {
        return waiting;
    }

    public Integer getRestartingWait() {
        return restartingWait;
    }

}
