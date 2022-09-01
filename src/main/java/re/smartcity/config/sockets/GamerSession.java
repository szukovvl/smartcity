package re.smartcity.config.sockets;

import org.springframework.web.reactive.socket.WebSocketSession;

public final class GamerSession {

    private WebSocketSession session;
    private final int key;

    public GamerSession(int key) {
        this.key = key;
    }

    public WebSocketSession getSession() {
        return session;
    }

    public void setSession(WebSocketSession session) {
        this.session = session;
    }

    public int getKey() {
        return key;
    }
}
