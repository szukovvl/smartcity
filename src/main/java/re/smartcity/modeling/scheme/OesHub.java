package re.smartcity.modeling.scheme;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import re.smartcity.energynet.IComponentIdentification;

public final class OesHub implements IOesHub {

    @JsonIgnore
    private final IComponentIdentification owner;

    private final int address;

    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    private final IConnectionPort[] inputs;

    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    private String error;

    private OesHub(IComponentIdentification oes) {
        this.owner = oes;
        this.address = oes.getDevaddr();
        this.inputs = new IConnectionPort[] {
                new OesPort(oes.getDevaddr(), this)
        };
    }

    //region IOesHub
    @Override
    public boolean hasOwner() { return this.owner != null; }

    @Override
    public IComponentIdentification getOwner() { return this.owner; }

    @Override
    public int getAddress() { return this.address; }

    @Override
    public boolean supportControlPort() { return false; }

    @Override
    public IConnectionPort getControlPort() { return null; }

    @Override
    public boolean supportInputs() { return this.inputs != null; }

    @Override
    public IConnectionPort[] getInputs() { return this.inputs; }

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
        return this.inputs[0].getAddress() == address ? this.inputs[0] : null;
    }
    //endregion

    public static OesHub create(IComponentIdentification oes) {
        return new OesHub(oes);
    }

}
