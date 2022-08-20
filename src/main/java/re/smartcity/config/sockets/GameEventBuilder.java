package re.smartcity.config.sockets;

public class GameEventBuilder<T> {

    private GameEventTypes type;
    private T data;

    public GameEventBuilder<T> type(GameEventTypes type) {
        this.type = type;
        return this;
    }

    public GameEventBuilder<T> data(T data) {
        this.data = data;
        return this;
    }

    public GameServiceEvent<T> build() {
        return new GameServiceEvent<>(type, data);
    }
}
