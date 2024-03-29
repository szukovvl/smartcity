package re.smartcity.modeling.scheme;

import re.smartcity.config.sockets.process.GameValues;

public interface IConnectionPort {
    int getAddress(); // физический адрес точки подключения
    IOesHub getOwner(); // владелец точки
    boolean hasError();
    String getError();
    void setError(String error);
    IConnectionPort[] getConnections();
    void setConnections(IConnectionPort[] connections);
    boolean addConnection(IConnectionPort port);
    boolean isOn();
    void setOn(boolean ison);
}
