package re.smartcity.config.sockets;

public interface ICommonEvent<T> {

    CommonEventTypes getType();
    int getId();
    long getTimestamp();
    T getData();

}
