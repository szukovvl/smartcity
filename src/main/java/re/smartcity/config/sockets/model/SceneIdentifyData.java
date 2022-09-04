package re.smartcity.config.sockets.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@Data
public final class SceneIdentifyData {

    private String commandname;

    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    private String slogan;

    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    private String notice;

    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    private SceneIdentifyPartner[] partners;
}
