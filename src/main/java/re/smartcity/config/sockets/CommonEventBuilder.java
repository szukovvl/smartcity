package re.smartcity.config.sockets;

public class CommonEventBuilder<T> {

    private CommonEventTypes type;
    private T data;

    public CommonEventBuilder<T> type(CommonEventTypes type) {
        this.type = type;
        return this;
    }

    public CommonEventBuilder<T> data(T data) {
        this.data = data;
        return this;
    }

    public CommonServiceEvent<T> build() {
        return new CommonServiceEvent<>(type, data);
    }
}
