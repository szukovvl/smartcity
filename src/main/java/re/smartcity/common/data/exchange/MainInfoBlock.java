package re.smartcity.common.data.exchange;

import lombok.Data;
import lombok.NoArgsConstructor;
import re.smartcity.sun.SunStatusData;
import re.smartcity.wind.WindStatusData;

@Data
@NoArgsConstructor
public class MainInfoBlock {
    private WindStatusData windData;
    private SunStatusData sunData;
}
