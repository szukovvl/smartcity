package re.smartcity.modeling.data;

import re.smartcity.energynet.component.Consumer;
import re.smartcity.energynet.component.EnergyDistributor;

public class GamerScenesData {

    private Consumer[] predefconsumers = new Consumer[0];
    private final EnergyDistributor substation;

    public GamerScenesData (EnergyDistributor substation) {
        this.substation = substation;
    }

    public Consumer[] getPredefconsumers() {
        return predefconsumers;
    }

    public void setPredefconsumers(Consumer[] predefconsumers) {
        this.predefconsumers = predefconsumers;
    }

    public EnergyDistributor getSubstation() {
        return substation;
    }
}