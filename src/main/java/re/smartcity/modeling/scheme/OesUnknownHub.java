package re.smartcity.modeling.scheme;

import com.fasterxml.jackson.annotation.JsonInclude;
import re.smartcity.energynet.IComponentIdentification;
import re.smartcity.energynet.component.ElectricalSubnet;

public final class OesUnknownHub implements IOesHub {

    private final int address;

    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    private String error;

    private IConnectionPort port;

    private OesUnknownHub(int address) {
        this.address = address;
        this.port = new OesPort(address, this);
    }

    //region IOesHub
    @Override
    public boolean hasOwner() { return false; }

    @Override
    public IComponentIdentification getOwner() { return null; }

    @Override
    public int getAddress() { return this.address; }

    @Override
    public boolean supportControlPort() { return false; }

    @Override
    public IConnectionPort getControlPort() { return null; }

    @Override
    public boolean supportInputs() { return true; }

    @Override
    public IConnectionPort[] getInputs() {
        return new IConnectionPort[] { this.port };
    }

    @Override
    public boolean supportOutputs() { return false; }

    @Override
    public IConnectionPort[] getOutputs() { return null; }

    @Override
    public boolean hasError() { return this.error != null; }

    @Override
    public String getError() { return this.error; }

    @Override
    public void setError(String error) { this.error = error; }

    @Override
    public boolean itIsMine(int address) {
        return this.address == address;
    }

    @Override
    public IConnectionPort connectionByAddress(int address) {
        return this.address == address ? this.port : null;
    }
    //endregion

    public static OesUnknownHub create(int addrress) {
        return new OesUnknownHub(addrress);
    }
}
