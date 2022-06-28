package re.smartcity.stand;

public class StandControlData {

    volatile private Integer waiting = 500; // ожидание между опросом ПЛК
    volatile private Integer restartingWait = 3000; // ожидание при перезапуске, после останова сервиса

    public Integer getWaiting() {
        return waiting;
    }

    public Integer getRestartingWait() {
        return restartingWait;
    }

}
