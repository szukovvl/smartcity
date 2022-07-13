package re.smartcity.common.data.exchange;

import lombok.Data;
import lombok.NoArgsConstructor;
import re.smartcity.common.data.ForecastPoint;

@Data
@NoArgsConstructor
public class ForecastInterpolation {
    boolean isLinear = false;
    ForecastPoint[] items;
}
