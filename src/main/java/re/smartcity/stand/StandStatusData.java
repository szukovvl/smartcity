package re.smartcity.stand;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import re.smartcity.wind.WindServiceStatuses;

@Data
public class StandStatusData {
    volatile private WindServiceStatuses status = WindServiceStatuses.STOPPED;

    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    volatile private String errorMsg;
}
