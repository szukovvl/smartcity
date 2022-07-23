package re.smartcity.common.data.exchange;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class TariffThreeZones {

    private double peak = 0.0;
    private double pp = 0.0;
    private double night = 0.0;

}
