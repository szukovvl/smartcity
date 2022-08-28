package re.smartcity.config.sockets.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public final class GameStartScenesEvent_Data {
    private byte mainstation;

    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    private byte substation = 0;

    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    private int[] consumers = null;
}
