package re.smartcity.config.sockets;

import re.smartcity.config.sockets.CommonEventTypes;

public interface IGameEvent<T> {
    GameEventTypes getType();
    int getId();
    long getTimestamp();
    T getData();
}
