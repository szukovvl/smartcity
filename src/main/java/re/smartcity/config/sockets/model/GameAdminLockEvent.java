package re.smartcity.config.sockets.model;

import com.fasterxml.jackson.annotation.JsonInclude;

public final class GameAdminLockEvent {
    private final boolean accepted;

    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    private final String token;

    public GameAdminLockEvent(boolean accepted, String token) {
        this.accepted = accepted;
        this.token = token;
    }

    public GameAdminLockEvent() {
        this(false, null);
    }

    public boolean isAccepted() {
        return accepted;
    }

    public String getToken() {
        return token;
    }
}
