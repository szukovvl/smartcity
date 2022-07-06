package re.smartcity.energynet.component.data.client;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;
import re.smartcity.common.data.Forecast;
import re.smartcity.common.resources.Messages;

@Data
@NoArgsConstructor
public class SmallConsumerSpecification {

    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    private volatile Forecast forecast; // прогноз

    private volatile boolean useforecast; // задействовать прогноз

    private volatile double energy; // максимальная мощность в МВт

    public static void validate(SmallConsumerSpecification data) {
        if (data.getEnergy() < 0.0) {
            throw new IllegalArgumentException(Messages.ER_1);
        }
        if (data.isUseforecast() && data.getForecast() == null) {
            throw new IllegalArgumentException(Messages.ER_2);
        }
    }
}
