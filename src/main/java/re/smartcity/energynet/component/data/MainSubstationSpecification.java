package re.smartcity.energynet.component.data;

import re.smartcity.energynet.ISpecifications;
import re.smartcity.energynet.component.ElectricalSubnet;

public class MainSubstationSpecification implements ISpecifications {

    public MainSubstationSpecification() { }

    private volatile ElectricalSubnet[] inputs; // подсети ввода энергии только генерация
    private volatile ElectricalSubnet[] outputs; // подсети потребителей только потребление
    private volatile double external_energy = 0; // внешний стабильный источник энергии
    private volatile double carbon = 684.75; // г/кВт*ч только для внешней энергии

    //region характеристики
    public ElectricalSubnet[] getInputs() {
        return inputs;
    }

    public void setInputs(ElectricalSubnet[] inputs) {
        this.inputs = inputs;
    }

    public ElectricalSubnet[] getOutputs() {
        return outputs;
    }

    public void setOutputs(ElectricalSubnet[] outputs) {
        this.outputs = outputs;
    }

    public double getExternal_energy() {
        return external_energy;
    }

    public void setExternal_energy(double external_energy) {
        this.external_energy = external_energy;
    }

    public double getCarbon() {
        return carbon;
    }

    public void setCarbon(double carbon) {
        this.carbon = carbon;
    }
    //endregion

    public static MainSubstationSpecification createDefault(String pref, byte[] inputs, byte[] outputs) {
        MainSubstationSpecification res = new MainSubstationSpecification();

        ElectricalSubnet[] lines = new ElectricalSubnet[inputs.length];
        for (int i = 0; i < inputs.length; i++) {
            lines[i] = ElectricalSubnet.create(pref + "-Лг-" + (i + 1), inputs[i]);
        }
        res.setInputs(lines);

        lines = new ElectricalSubnet[outputs.length];
        for (int i = 0; i < outputs.length; i++) {
            lines[i] = ElectricalSubnet.create(pref + "-Лп-" + (i + 1), outputs[i]);
        }
        res.setOutputs(lines);

        return res;
    }
}
