package re.smartcity.common.data.exchange;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class TransmissionServices {

    private double tk_high = 0.0;
    private double tk_avg_1 = 0.0;
    private double tk_avg_2 = 0.0;
    private double tk_low = 0.0;
    private double tk_mid = 0.0;

}
