package re.smartcity.energynet.component.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import re.smartcity.energynet.IComponentManagement;
import re.smartcity.energynet.component.ElectricalSubnet;
import re.smartcity.energynet.IGeneration;

public class EnergyDistributorSpecification implements IComponentManagement {

    @JsonIgnore
    private volatile boolean isactive;

    @JsonIgnore
    private volatile EnergyDistribotorInstantValues instantValues;

    @JsonIgnore
    private volatile EnergyDistributorStackedValues stackedValues;

    public EnergyDistributorSpecification() { }

    // private ElectricalSubnet energyinput; // подсеть ввода энергии (?) данный объект просто помещается в узел
    // может сделать небольшой блок данных, содержащий информацию о узле сети?
    private ElectricalSubnet[] outputs; // подсети потребителей

    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    private IGeneration generation; // собственная генерация

    //region IComponentManagement
    @Override
    public boolean getIsactive() { return this.isactive; }

    @Override
    public void setIsactive(boolean isactive) { this.isactive = isactive; }
    //endregion

    //region характеристики
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

    //region оперативные данные модели
    //region мгновенные значения
    public EnergyDistribotorInstantValues getInstantValues() {
        return instantValues;
    }

    public void setInstantValues(EnergyDistribotorInstantValues instantValues) {
        this.instantValues = instantValues;
    }
    //endregion

    //region значения с накоплением
    public EnergyDistributorStackedValues getStackedValues() {
        return stackedValues;
    }

    public void setStackedValues(EnergyDistributorStackedValues stackedValues) {
        this.stackedValues = stackedValues;
    }
    //endregion
    //endregion

    public static EnergyDistributorSpecification createDefault(String pref) {
        EnergyDistributorSpecification res = new EnergyDistributorSpecification();
        res.setOutputs(new ElectricalSubnet[] {
                ElectricalSubnet.create(pref + "-Лп-1"),
                ElectricalSubnet.create(pref + "-Лп-2")
        });
        return res;
    }
}
