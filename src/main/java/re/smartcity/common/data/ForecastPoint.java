package re.smartcity.common.data;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Date;

@Data
@AllArgsConstructor
public class ForecastPoint {
    private Date point;
    private Double value;
}
