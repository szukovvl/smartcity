package re.smartcity.modeling.scheme;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import re.smartcity.energynet.IComponentIdentification;
import re.smartcity.energynet.component.Consumer;

import java.util.Arrays;

public final class OesConsumerHub implements IOesHub {

    @JsonIgnore
    private final IComponentIdentification owner;

    private final int address;

    private final IConnectionPort[] inputs;

    private boolean alien;

    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    private String error;

    private OesConsumerHub(Consumer oes) {
        this.owner = oes;
        this.address = oes.getDevaddr();
        this.inputs = Arrays.stream(oes.getData().getInputs())
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
        return this.address == address ||
                Arrays.stream(this.inputs).anyMatch(e -> e.getAddress() == address);
    }

    @Override
    public IConnectionPort connectionByAddress(int address) {
        return Arrays.stream(this.inputs)
                .filter(e -> e.getAddress() == address)
                .findFirst()
                .orElse(null);
    }

    @Override
    public boolean isAlien() { return this.alien; }

    @Override
    public void setAlien(boolean alien) { this.alien = alien; }
    //endregion

    public static OesConsumerHub create(Consumer oes) {
        return new OesConsumerHub(oes);
    }

}
