package re.smartcity.common.data.exchange;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class AdditionalGenerationTariff {

    private double resource = 0.0;
    private double sun = 0.0;
    private double wind = 0.0;
    private double storage = 0.0;

}
