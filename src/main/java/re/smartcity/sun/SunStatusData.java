package re.smartcity.sun;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import re.smartcity.common.data.Forecast;
import re.smartcity.common.data.exchange.SimpleSunData;

@Data
public class SunStatusData {
    volatile private int power = 0;
    volatile private boolean isOn = false;

    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    private Forecast forecast; // прогноз

    private boolean useforecast; // задействовать прогноз

    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    volatile private String errorMsg;

    public void apply(SimpleSunData src) {
        setPower(src.getPower());
        setForecast(src.getForecast());
        setUseforecast(src.isUseforecast());
    }
}
