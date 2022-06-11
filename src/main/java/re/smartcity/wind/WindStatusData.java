package re.smartcity.wind;

import lombok.Data;

@Data
public class WindStatusData {
    volatile private WindServiceStatuses status = WindServiceStatuses.STOPPED;
    volatile private Integer power = 0;
    volatile private boolean isOn = false;
    volatile private String errorMsg;
}
