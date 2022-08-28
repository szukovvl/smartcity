package re.smartcity.config.sockets;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.ToString;

@ToString
public final class GameClientEvent {

    private final GameEventTypes type;

    private final String payload;

    @JsonCreator
    public GameClientEvent(@JsonProperty("type") GameEventTypes type,
                 @JsonProperty("payload") String payload) {
        this.type = type;
        this.payload = payload;
    }

    public GameEventTypes getType() {
        return type;
    }

    public String getPayload() {
        return payload;
    }
}
