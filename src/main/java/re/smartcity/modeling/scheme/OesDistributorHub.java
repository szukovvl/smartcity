package re.smartcity.modeling.scheme;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import re.smartcity.energynet.IComponentIdentification;
import re.smartcity.energynet.component.EnergyDistributor;

import java.util.Arrays;

public final class OesDistributorHub implements IOesHub {

    @JsonIgnore
    private final IComponentIdentification owner;

    private final int address;

    private final IConnectionPort[] inputs;

    private final IConnectionPort[] outputs;

    private boolean alien;

    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    private String error;

    private OesDistributorHub(EnergyDistributor oes) {
        this.owner = oes;
        this.address = oes.getDevaddr();
        this.inputs = new IConnectionPort[] {
                new OesPort(oes.getData().getInaddr(), this)
        };
        this.outputs = Arrays.stream(oes.getData().getOutputs())
                .map(e -> new OesPort(this, e))
                .toArray(IConnectionPort[]::new);
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
    public boolean supportOutputs() { return this.outputs != null; }

    @Override
    public IConnectionPort[] getOutputs() { return this.outputs; }

    @Override
    public boolean hasError() { return this.error != null; }

    @Override
    public String getError() { return this.error; }

    @Override
    public void setError(String error) { this.error = error; }

    @Override
    public boolean itIsMine(int address) {
        return this.address == address ||
                this.inputs[0].getAddress() == address ||
                Arrays.stream(this.outputs).anyMatch(e -> e.getAddress() == address);
    }

    @Override
    public IConnectionPort connectionByAddress(int address) {
        if (this.inputs[0].getAddress() == address) {
            return this.inputs[0];
        }
        return Arrays.stream(this.outputs)
                .filter(e -> e.getAddress() == address)
                .findFirst()
                .orElse(null);
    }

    @Override
    public boolean isAlien() { return this.alien; }

    @Override
    public void setAlien(boolean alien) { this.alien = alien; }
    //endregion

    public static OesDistributorHub create(EnergyDistributor oes) {
        return new OesDistributorHub(oes);
    }
}
