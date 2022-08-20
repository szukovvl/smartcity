package re.smartcity.config.sockets;

import java.util.concurrent.atomic.AtomicInteger;

public class GameServiceEvent<T> implements IGameEvent<T> {

    private static final AtomicInteger ID_GENERATOR = new AtomicInteger(0);

    private final GameEventTypes type;
    private final int id;
    private final T data;
    private final long timestamp;

    public GameServiceEvent(GameEventTypes type, T data) {
        this.type = type;
        this.id = ID_GENERATOR.addAndGet(1);
        this.data = data;
        this.timestamp = System.currentTimeMillis();
    }

    //region ICommonEvent
    @Override
    public GameEventTypes getType() {
        return this.type;
    }

    @Override
    public int getId() {
        return this.id;
    }

    @Override
    public long getTimestamp() {
        return this.timestamp;
    }

    @Override
    public T getData() {
        return this.data;
    }
    //endregion

    public static <T> GameEventBuilder<T> type(GameEventTypes type) {
        return new GameEventBuilder<T>().type(type);
    }
}
