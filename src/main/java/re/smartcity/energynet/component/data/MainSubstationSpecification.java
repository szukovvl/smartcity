package re.smartcity.energynet.component.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import re.smartcity.energynet.IComponentManagement;
import re.smartcity.energynet.component.ElectricalSubnet;

public class MainSubstationSpecification implements IComponentManagement {

    @JsonIgnore
    private volatile boolean isactive;

    @JsonIgnore
    private volatile MainSubstationInstantValues instantValues;

    @JsonIgnore
    private volatile MainSubstationStackedValues stackedValues;

    public MainSubstationSpecification() { }

    private volatile ElectricalSubnet[] inputs; // подсети ввода энергии только генерация
    private volatile ElectricalSubnet[] outputs; // подсети потребителей только потребление
    private volatile double external_energy = 0; // внешний стабильный источник энергии
    private volatile double tariff = 0.0; // тариф на внешний источник энергии (ВИЭ)
    private volatile double carbon = 684.75; // г/кВт*ч только для внешней энергии

    //region IComponentManagement
    @Override
    public boolean getIsactive() { return this.isactive; }

    @Override
    public void setIsactive(boolean isactive) { this.isactive = isactive; }
    //endregion

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

    public double getTariff() {
        return tariff;
    }

    public void setTariff(double tariff) {
        this.tariff = tariff;
    }

    public double getCarbon() {
        return carbon;
    }

    public void setCarbon(double carbon) {
        this.carbon = carbon;
    }
    //endregion

    //region оперативные данные модели
    //region мгновенные значения
    public MainSubstationInstantValues getInstantValues() {
        return instantValues;
    }

    public void setInstantValues(MainSubstationInstantValues instantValues) {
        this.instantValues = instantValues;
    }
    //endregion

    //region значения с накоплением
    public MainSubstationStackedValues getStackedValues() {
        return stackedValues;
    }

    public void setStackedValues(MainSubstationStackedValues stackedValues) {
        this.stackedValues = stackedValues;
    }
    //endregion
    //endregion

    public static MainSubstationSpecification createDefault(String pref) {
        MainSubstationSpecification res = new MainSubstationSpecification();
        res.setInputs(new ElectricalSubnet[] {
                ElectricalSubnet.create(pref + "-Лг-1"),
                ElectricalSubnet.create(pref + "-Лг-2"),
                ElectricalSubnet.create(pref + "-Лг-3")
        });
        res.setOutputs(new ElectricalSubnet[] {
                ElectricalSubnet.create(pref + "-Лп-1"),
                ElectricalSubnet.create(pref + "-Лп-2"),
                ElectricalSubnet.create(pref + "-Лп-3")
        });
        return res;
    }
}
