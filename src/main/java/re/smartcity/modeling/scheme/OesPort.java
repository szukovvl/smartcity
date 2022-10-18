package re.smartcity.modeling.scheme;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import re.smartcity.energynet.component.ElectricalSubnet;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class OesPort implements IConnectionPort {

    private final int address;

    @JsonIgnore
    private final IOesHub owner;

    // @JsonIgnore
    // private final ElectricalSubnet subnet;

    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    private IConnectionPort[] connections;

    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    private String error;

    public OesPort(int address, IOesHub owner) {
        this.address = address;
        this.owner = owner;
        // this.subnet = null;
    }

    public OesPort(IOesHub owner, ElectricalSubnet subnet) {
        this.owner = owner;
        // this.subnet = subnet;
        this.address = subnet.getDevaddr();
    }

    //region IConnectionPort
    @Override
    public int getAddress() { return this.address; }

    @Override
    public IOesHub getOwner() { return this.owner; }

    // @Override
    // public boolean hasSubnet() { return this.subnet != null; }

    // @Override
    // public ElectricalSubnet getSubnet() { return this.subnet; }

    @Override
    public boolean hasError() { return this.error != null; }

    @Override
    public String getError() { return this.error; }

    @Override
    public void setError(String error) { this.error = error; }

    @Override
    public IConnectionPort[] getConnections() { return this.connections; }

    @Override
    public void setConnections(IConnectionPort[] connections) { this.connections = connections; }

    @Override
    public boolean addConection(IConnectionPort port) {
        if (port == null) {
            return false;
        }
        if (this.connections != null) {
            if (Arrays.stream(this.connections)
                    .anyMatch(e -> e.getAddress() == port.getAddress())) {
                return false;
            }
        }
        List<IConnectionPort> items = new ArrayList<>(List.of(
                this.connections != null ? this.connections : new IConnectionPort[0]));
        items.add(port);
        this.setConnections(items.toArray(IConnectionPort[]::new));
        return true;
    }
    //endregion
}
