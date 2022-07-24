package re.smartcity.common.data.exchange;

import lombok.Data;
import lombok.NoArgsConstructor;
import re.smartcity.common.data.ForecastPoint;

@Data
@NoArgsConstructor
public class ForecastInterpolation {
    private boolean isLinear = false;
    private ForecastPoint[] items = new ForecastPoint[0];
}
