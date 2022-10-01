package re.smartcity.energynet.component.data;

import re.smartcity.energynet.GenerationUsageModes;
import re.smartcity.energynet.ISpecifications;
import re.smartcity.energynet.SupportedGenerations;

public class GreenGenerationSpecification implements ISpecifications {

    public GreenGenerationSpecification() { }

    private volatile double energy = 0.1; // максимальная мощность в МВт
    private volatile SupportedGenerations generation_type; // тип генерации
    private volatile double highload = 0.8; // значение в процента от генерируемой мощности, высокая нагрузка
    private volatile double criticalload = 0.95; // значение в процентах от генерируемой мощности, критическая нагрузка
    private volatile int blackouttime = 300; // время в секундах, прежде чем произойдет отключение генерации
    private volatile double carbon = 700.0; // г/кВт*ч
    private volatile GenerationUsageModes mode = GenerationUsageModes.ALWAYS; // режим использования

    //region характеристики
    public double getEnergy() {
        return energy;
    }

    public void setEnergy(double energy) {
        this.energy = energy;
    }

    public SupportedGenerations getGeneration_type() {
        return generation_type;
    }

    public void setGeneration_type(SupportedGenerations generation_type) {
        this.generation_type = generation_type;
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

    public static GreenGenerationSpecification createDefault(SupportedGenerations generation) {
        GreenGenerationSpecification res = new GreenGenerationSpecification();
        res.setGeneration_type(generation);
        switch (generation) {
            case WIND -> res.setCarbon(6.9);
            case SOLAR -> res.setCarbon(18.0);
        }
        return res;
    }
}
