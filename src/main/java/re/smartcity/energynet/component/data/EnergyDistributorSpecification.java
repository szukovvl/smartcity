package re.smartcity.energynet.component.data;

import com.fasterxml.jackson.annotation.JsonInclude;
import re.smartcity.energynet.ISpecifications;
import re.smartcity.energynet.component.ElectricalSubnet;
import re.smartcity.energynet.IGeneration;

public class EnergyDistributorSpecification implements ISpecifications {

    public EnergyDistributorSpecification() { }

    private ElectricalSubnet inputline; // !!! входная линия

    private ElectricalSubnet[] outputs; // подсети потребителей

    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    private IGeneration generation; // собственная генерация

    //region характеристики
    public ElectricalSubnet getInputline() {
        return inputline;
    }

    public void setInputline(ElectricalSubnet inputline) {
        this.inputline = inputline;
    }

    public ElectricalSubnet[] getOutputs() {
        return outputs;
    }

    public void setOutputs(ElectricalSubnet[] outputs) {
        this.outputs = outputs;
    }

    public IGeneration getGeneration() {
        return generation;
    }

    public void setGeneration(IGeneration generation) {
        this.generation = generation;
    }
    //endregion

    public static EnergyDistributorSpecification createDefault(String pref, byte inlineaddr, byte[] lines) {
        EnergyDistributorSpecification res = new EnergyDistributorSpecification();

        res.setInputline(ElectricalSubnet.create(pref + "Вх", inlineaddr));

        ElectricalSubnet[] sublines = new ElectricalSubnet[lines.length];
        for (int i = 0; i < lines.length; i++) {
            sublines[i] = ElectricalSubnet.create(pref + "-Лп-" + (i + 1), lines[i]);
        }
        res.setOutputs(sublines);

        return res;
    }
}
