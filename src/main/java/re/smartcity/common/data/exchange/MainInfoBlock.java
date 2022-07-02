package re.smartcity.common.data.exchange;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;
import re.smartcity.energynet.SupportedTypes;
import re.smartcity.stand.StandStatusData;
import re.smartcity.sun.SunStatusData;
import re.smartcity.wind.WindStatusData;

import java.util.Map;

@Data
@NoArgsConstructor
public class MainInfoBlock {
    private WindStatusData windData;
    private SunStatusData sunData;
    private StandStatusData standStatus;

    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    private Map<String, Integer> elements;
}
