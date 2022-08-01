package re.smartcity.wind;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@Data
public class WindStatusData {

    volatile private int power = 0;
    volatile private boolean isOn = false;
    volatile private String url = "";

    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    volatile private String errorMsg;
}
