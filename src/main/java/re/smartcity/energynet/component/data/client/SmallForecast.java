package re.smartcity.energynet.component.data.client;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import re.smartcity.common.data.ForecastPoint;

@Data
@NoArgsConstructor
public class SmallForecast {
    @NonNull
    private String name;
    @NonNull
    private ForecastPoint[] data;
}
