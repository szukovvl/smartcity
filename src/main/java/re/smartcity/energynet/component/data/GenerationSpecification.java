package re.smartcity.energynet.component.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import re.smartcity.common.data.Forecast;
import re.smartcity.energynet.GenerationUsageModes;
import re.smartcity.energynet.IComponentManagement;

public class GenerationSpecification implements IComponentManagement {

    @JsonIgnore
    private volatile boolean isactive;

    @JsonIgnore
    private GenerationInstantValues instantValues;

    @JsonIgnore
    private GenerationStackedValues stackedValues;

    public GenerationSpecification() { }

    private Forecast forecast; // прогноз
    private boolean useforecast = false; // задействовать прогноз
    private double energy = 1.0; // максимальная мощность в МВт
    private double highload = 0.8; // значение в процента от генерируемой мощности, высокая нагрузка
    private double criticalload = 0.9; // значение в процентах от генерируемой мощности, критическая нагрузка
    private int blackouttime = 300; // время в секундах, прежде чем произойдет отключение генерации
    private double tariff; // тариф
    private double carbon = 930; // г/кВт*ч
    private GenerationUsageModes mode = GenerationUsageModes.RESERVE; // режим использования

    //region IComponentManagement
    @Override
    public boolean getIsactive() { return this.isactive; }

    @Override
    public void setIsactive(boolean isactive) { this.isactive = isactive; }
    //endregion

    //region характеристики
    public Forecast getForecast() {
        return forecast;
    }

    public void setForecast(Forecast forecast) {
        if (forecast == null) {
            setUseforecast(false);
        }
        this.forecast = forecast;
    }

    public boolean isUseforecast() {
        return useforecast;
    }

    public void setUseforecast(boolean useforecast) {
        if (getForecast() == null) {
            this.useforecast = false;
        } else {
            this.useforecast = useforecast;
        }
    }

    public double getEnergy() {
        return energy;
    }

    public void setEnergy(double energy) {
        this.energy = energy;
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

    public GenerationUsageModes getMode() {
        return mode;
    }

    public void setMode(GenerationUsageModes mode) {
        this.mode = mode;
    }
    //endregion

    //region оперативные данные модели
    //region мгновенные значения
    public GenerationInstantValues getInstantValues() {
        return instantValues;
    }

    public void setInstantValues(GenerationInstantValues instantValues) {
        this.instantValues = instantValues;
    }
    //endregion

    //region значения с накоплением
    public GenerationStackedValues getStackedValues() {
        return stackedValues;
    }

    public void setStackedValues(GenerationStackedValues stackedValues) {
        this.stackedValues = stackedValues;
    }
    //endregion
    //endregion

    public static GenerationSpecification createDefault() {
        return new GenerationSpecification();
    }
}
