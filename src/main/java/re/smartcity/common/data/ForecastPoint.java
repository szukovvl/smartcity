package re.smartcity.common.data;

import lombok.Data;

import java.util.Date;

@Data
public class ForecastPoint {
    private Date point;
    private Double value;
}
