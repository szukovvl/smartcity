package re.smartcity.config.sockets.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import re.smartcity.modeling.GameStatuses;

public final class GamerEnterEvent {

    private final boolean accept;

    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    private final String token;

    private final GameStatuses gameStatus;

    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    private final int key;

    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    private final ResponseScenesEventData data;

    public GamerEnterEvent(boolean accept, String token, GameStatuses gameStatus, int key, ResponseScenesEventData data) {
        this.accept = accept;
        this.token = token;
        this.gameStatus = gameStatus;
        this.key = key;
        this.data = data;
    }

    public GamerEnterEvent(GameStatuses gameStatus) {
        this(false, null, gameStatus, 0, null);
    }

    public boolean isAccept() {
        return accept;
    }

    public String getToken() {
        return token;
    }

    public GameStatuses getGameStatus() {
        return gameStatus;
    }

    public int getKey() {
        return key;
    }

    public ResponseScenesEventData getData() {
        return data;
    }
}
