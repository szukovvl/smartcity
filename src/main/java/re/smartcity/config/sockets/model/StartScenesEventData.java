package re.smartcity.config.sockets.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public final class StartScenesEventData {
    private int mainstation;
    private int substation;

    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    private int[] consumers = null;
}
