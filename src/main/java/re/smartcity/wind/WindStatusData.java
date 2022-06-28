package re.smartcity.wind;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@Data
public class WindStatusData {
    volatile private WindServiceStatuses status = WindServiceStatuses.STOPPED;
    volatile private Integer power = 0;
    volatile private boolean isOn = false;

    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    volatile private String errorMsg;
}
