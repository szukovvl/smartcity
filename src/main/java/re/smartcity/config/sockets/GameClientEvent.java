package re.smartcity.config.sockets;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public record GameClientEvent(GameEventTypes type) {
    @JsonCreator
    public GameClientEvent(@JsonProperty("type") GameEventTypes type) {
        this.type = type;
    }
}
