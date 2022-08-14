package re.smartcity.wind;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import re.smartcity.common.data.Forecast;
import re.smartcity.common.data.exchange.SimpleWindData;

@Data
public class WindStatusData {

    volatile private int power = 0;
    volatile private boolean isOn = false;
    volatile private String url = "";

    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    private Forecast forecast; // прогноз

    private boolean useforecast; // задействовать прогноз

    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    volatile private String errorMsg;

    public void apply(SimpleWindData data) {
        setUrl(data.getUrl());
        setPower(data.getPower());
        setForecast(data.getForecast());
        setUseforecast(data.isUseforecast());
    }
}
