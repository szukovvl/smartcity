package re.smartcity.common.data.exchange;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;
import re.smartcity.common.data.Forecast;

@Data
@NoArgsConstructor
public class SmallConsumerSpecification {

    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    private volatile Forecast forecast; // прогноз

    private volatile boolean useforecast = false; // задействовать прогноз

    private volatile double energy = 0.8; // максимальная мощность в МВт

}
