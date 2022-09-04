package re.smartcity.config.sockets.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@Data
public final class SceneIdentifyPartner {

    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    private String role;

    private String name;

    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    private String youfrom;
}
