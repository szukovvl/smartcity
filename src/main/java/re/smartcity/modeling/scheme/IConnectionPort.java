package re.smartcity.modeling.scheme;

import re.smartcity.energynet.component.ElectricalSubnet;

public interface IConnectionPort {
    int getAddress(); // физический адрес точки подключения
    IOesHub getOwner(); // владелец точки
    // boolean hasSubnet();
    // ElectricalSubnet getSubnet(); // линия-владелец
    boolean hasError();
    String getError();
    void setError(String error);
    IConnectionPort[] getConnections();
    void setConnections(IConnectionPort[] connections);
    boolean addConection(IConnectionPort port);
}
