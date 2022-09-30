package re.smartcity.energynet.component.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import re.smartcity.energynet.IComponentIdentification;
import re.smartcity.energynet.IComponentManagement;

public class ElectricalSubnetSpecification implements IComponentManagement {

    @JsonIgnore
    private volatile boolean isactive;

    @JsonIgnore
    private volatile ElectricalSubnetInstantValues instantValues;

    @JsonIgnore
    private volatile ElectricalSubnetStackedValues stackedValues;

    public ElectricalSubnetSpecification() { }

    private double energy = 1.0; // максимальная мощность в МВт
    private volatile double lossfactor = 0.9; // потери в сети (устанавливается при первой инициализации)

    @JsonProperty(value = "components", access = JsonProperty.Access.READ_ONLY)
    private volatile IComponentIdentification[] components; // компоненты электросети, подключенные к данной подсети

    private volatile double highload = 0.8; // значение в процентах от мощности, высокая нагрузка (80%)
    private volatile double criticalload = 0.95; // значение в процентах от мощности, критическая нагрузка (95)
    private volatile int blackouttime = 300; // время в секундах, прежде чем произойдет отключение

    //region IComponentManagement
    @Override
    public boolean getIsactive() { return this.isactive; }

    @Override
    public void setIsactive(boolean isactive) { this.isactive = isactive; }
    //endregion

    public double getEnergy() {
        return energy;
    }

    public void setEnergy(double energy) {
        this.energy = energy;
    }

    public double getLossfactor() {
        return lossfactor;
    }

    public void setLossfactor(double lossfactor) {
        this.lossfactor = lossfactor;
    }

    public IComponentIdentification[] getComponents() {
        return components;
    }

    public void setComponents(IComponentIdentification[] components) {
        this.components = components;
    }

    public double getHighload() {
        return highload;
    }

    public void setHighload(double highload) {
        this.highload = highload;
    }

    public double getCriticalload() {
        return criticalload;
    }

    public void setCriticalload(double criticalload) {
        this.criticalload = criticalload;
    }

    public int getBlackouttime() {
        return blackouttime;
    }

    public void setBlackouttime(int blackouttime) {
        this.blackouttime = blackouttime;
    }


    //region оперативные данные модели
    //region мгновенные значения
    public ElectricalSubnetInstantValues getInstantValues() {
        return instantValues;
    }

    public void setInstantValues(ElectricalSubnetInstantValues instantValues) {
        this.instantValues = instantValues;
    }
    //endregion

    //region значения с накоплением
    public ElectricalSubnetStackedValues getStackedValues() {
        return stackedValues;
    }

    public void setStackedValues(ElectricalSubnetStackedValues stackedValues) {
        this.stackedValues = stackedValues;
    }
    //endregion
    //endregion

    public static ElectricalSubnetSpecification createDefault() {
        ElectricalSubnetSpecification res = new ElectricalSubnetSpecification();
        res.setComponents(new IComponentIdentification[] { });
        return res;
    }
}
