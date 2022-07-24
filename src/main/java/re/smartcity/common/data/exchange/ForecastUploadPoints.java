package re.smartcity.common.data.exchange;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;
import re.smartcity.common.data.ForecastPoint;

@Data
@NoArgsConstructor
public class ForecastUploadPoints {

    private ForecastPoint[] points = new ForecastPoint[0];

    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    private String errormsg;
}
