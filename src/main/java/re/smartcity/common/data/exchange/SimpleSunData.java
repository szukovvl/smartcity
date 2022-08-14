package re.smartcity.common.data.exchange;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import re.smartcity.common.data.Forecast;
import re.smartcity.common.resources.Messages;

@Data
@NoArgsConstructor
@AllArgsConstructor
public final class SimpleSunData {

    private int power = 0;

    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    private Forecast forecast; // прогноз

    private boolean useforecast; // задействовать прогноз

    public static void validate(SimpleSunData data) {
        if (data.getPower() < 0.0) {
            throw new IllegalArgumentException(Messages.ER_1);
        }
        if (data.isUseforecast() && data.getForecast() == null) {
            throw new IllegalArgumentException(Messages.ER_2);
        }
    }
}
