package re.smartcity.config.sockets.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ResponseScenesEventData {
    private int mainstation;
    private int substation;

    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    private int[] consumers = null;

    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    private SceneIdentifyData sceneidentify;

    public static ResponseScenesDataBuilder builder(int mainstation) {
        return new ResponseScenesDataBuilder(mainstation);
    }
}
