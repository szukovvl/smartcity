package re.smartcity.sun;

import lombok.Data;
import re.smartcity.wind.WindServiceStatuses;

@Data
public class SunStatusData {
    volatile private WindServiceStatuses status = WindServiceStatuses.STOPPED;
    volatile private Integer power = 0;
    volatile private boolean isOn = false;
    volatile private String errorMsg;
}
