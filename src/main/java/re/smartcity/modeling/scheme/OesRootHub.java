package re.smartcity.modeling.scheme;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import re.smartcity.energynet.IComponentIdentification;
import re.smartcity.energynet.component.Consumer;
import re.smartcity.energynet.component.EnergyDistributor;
import re.smartcity.energynet.component.MainSubstationPowerSystem;

import java.util.Arrays;

public final class OesRootHub implements IOesHub {

    @JsonIgnore
    private final IComponentIdentification owner;

    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    private int[] missed;

    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    private IOesHub[] devices;

    private final int address;

    private final IConnectionPort controlPort;

    private final IConnectionPort[] inputs;

    private final IConnectionPort[] outputs;

    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    private String error;

    private boolean connected = false;

    private OesRootHub(MainSubstationPowerSystem oes) {
        this.owner = oes;
        this.address = oes.getDevaddr();
        this.controlPort = new OesPort(oes.getData().getCtrladdr(), this);
        this.inputs = Arrays.stream(oes.getData().getInputs())
                .map(e -> new OesPort(this, e))
                .toArray(IConnectionPort[]::new);
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
    public boolean supportControlPort() { return this.controlPort != null; }

    @Override
    public IConnectionPort getControlPort() { return this.controlPort; }

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
                this.controlPort.getAddress() == address ||
                Arrays.stream(this.inputs).anyMatch(e -> e.getAddress() == address) ||
                Arrays.stream(this.outputs).anyMatch(e -> e.getAddress() == address);
    }

    @Override
    public IConnectionPort connectionByAddress(int address) {
        if (address == controlPort.getAddress())
            return controlPort;
        return Arrays.stream(this.inputs)
                .filter(e -> e.getAddress() == address)
                .findFirst()
                .orElse(Arrays.stream(this.outputs)
                        .filter(e -> e.getAddress() == address)
                        .findFirst()
                        .orElse(null));
    }

    @Override
    public boolean isAlien() { return false; }

    @Override
    public void setAlien(boolean alien) {  }
    //endregion


    public int[] getMissed() {
        return missed;
    }

    public void setMissed(int[] missed) {
        this.missed = missed;
    }

    public IOesHub[] getDevices() {
        return devices;
    }

    public void setDevices(IOesHub[] devices) {
        this.devices = devices;
    }

    public boolean isConnected() {
        return connected;
    }

    public void setConnected(boolean connected) {
        this.connected = connected;
    }

    public static OesRootHub create(MainSubstationPowerSystem oes) {
        return new OesRootHub(oes);
    }

    public static IOesHub createOther(IComponentIdentification oes) {
        return switch (oes.getComponentType()) {
            case DISTRIBUTOR -> OesDistributorHub.create((EnergyDistributor) oes);
            case CONSUMER -> OesConsumerHub.create((Consumer) oes);
            default -> OesHub.create(oes);
        };
    }

}
