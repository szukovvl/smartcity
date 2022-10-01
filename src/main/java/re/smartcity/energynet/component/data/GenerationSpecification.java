package re.smartcity.energynet.component.data;

import com.fasterxml.jackson.annotation.JsonInclude;
import re.smartcity.common.data.Forecast;
import re.smartcity.energynet.GenerationUsageModes;
import re.smartcity.energynet.ISpecifications;

public class GenerationSpecification implements ISpecifications {

    public GenerationSpecification() { }

    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    private Forecast forecast; // прогноз

    private boolean useforecast = false; // задействовать прогноз

    private double energy = 1.0; // максимальная мощность в МВт

    private double highload = 0.8; // значение в процента от генерируемой мощности, высокая нагрузка

    private double criticalload = 0.9; // значение в процентах от генерируемой мощности, критическая нагрузка

    private int blackouttime = 300; // время в секундах, прежде чем произойдет отключение генерации

    private double carbon = 930; // г/кВт*ч

    private GenerationUsageModes mode = GenerationUsageModes.RESERVE; // режим использования

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

    public static GenerationSpecification createDefault() {
        return new GenerationSpecification();
    }
}
