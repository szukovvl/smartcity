package re.smartcity.energynet.component.data;

import re.smartcity.energynet.ISpecifications;
import re.smartcity.energynet.component.ElectricalSubnet;

public class EnergyDistributorSpecification implements ISpecifications {

    public EnergyDistributorSpecification() { }

    private byte inaddr; // !!! входная линия

    private ElectricalSubnet[] outputs; // подсети потребителей

    //region характеристики
    public byte getInaddr() {
        return inaddr;
    }

    public void setInaddr(byte inaddr) {
        this.inaddr = inaddr;
    }

    public ElectricalSubnet[] getOutputs() {
        return outputs;
    }

    public void setOutputs(ElectricalSubnet[] outputs) {
        this.outputs = outputs;
    }
    //endregion

    public static EnergyDistributorSpecification createDefault(String pref, byte inlineaddr, byte[] lines) {
        EnergyDistributorSpecification res = new EnergyDistributorSpecification();

        res.setInaddr(inlineaddr);

        ElectricalSubnet[] sublines = new ElectricalSubnet[lines.length];
        for (int i = 0; i < lines.length; i++) {
            sublines[i] = ElectricalSubnet.create(pref + "-Лп-" + (i + 1), lines[i]);
        }
        res.setOutputs(sublines);

        return res;
    }
}
