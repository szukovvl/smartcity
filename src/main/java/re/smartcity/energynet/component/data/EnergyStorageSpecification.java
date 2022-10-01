package re.smartcity.energynet.component.data;

import re.smartcity.energynet.EnergyStorage_ChargeBehaviors;
import re.smartcity.energynet.EnergyStorage_States;
import re.smartcity.energynet.GenerationUsageModes;
import re.smartcity.energynet.ISpecifications;

public class EnergyStorageSpecification implements ISpecifications {

    public EnergyStorageSpecification() { }

    private volatile double energy = 1.1; // емкость в мВт*ч
    private volatile double performance = 0.88; // показатель эффективности системы хранения
    private volatile double peckertexponent = 1.1; // экспонента Пекерта
    private volatile double outpower = 0.5; // граница нормального значения мощности, отдаваемой потребителю, в процентах
    private volatile boolean overload_enabled = false; // разрешение превышения установленного параметра границы нормального значения отдаваемой мощности
    private volatile double maxdischarge = 0.2; // максимальная разрядка хранилища, в процентах
    private volatile double undercharging = 0.7; // недозарядка, когда устройство может быть вновь использовано
    private volatile double criticalload = 0.9; // критическое значение нагрузки на хранилище
    private volatile int blackouttime = 90; // время в секундах, прежде чем произойдет отключение хранилища
    private volatile double carbon = 798.9; // г/кВт*ч
    private volatile GenerationUsageModes mode = GenerationUsageModes.RESERVE; // режим использования
    private volatile EnergyStorage_ChargeBehaviors chargebehavior = EnergyStorage_ChargeBehaviors.LOWTARIFF; // поведение хранилища при восстановлении
    private volatile EnergyStorage_States initstate = EnergyStorage_States.CHARGED; // начальное состояние перед началом игрового процесса

    //region характеристики
    public double getEnergy() {
        return energy;
    }

    public void setEnergy(double energy) {
        this.energy = energy;
    }

    public double getPerformance() {
        return performance;
    }

    public void setPerformance(double performance) {
        this.performance = performance;
    }

    public double getPeckertexponent() {
        return peckertexponent;
    }

    public void setPeckertexponent(double peckertexponent) {
        this.peckertexponent = peckertexponent;
    }

    public double getOutpower() {
        return outpower;
    }

    public void setOutpower(double outpower) {
        this.outpower = outpower;
    }

    public boolean isOverload_enabled() {
        return overload_enabled;
    }

    public void setOverload_enabled(boolean overload_enabled) {
        this.overload_enabled = overload_enabled;
    }

    public double getMaxdischarge() {
        return maxdischarge;
    }

    public void setMaxdischarge(double maxdischarge) {
        this.maxdischarge = maxdischarge;
    }

    public double getUndercharging() {
        return undercharging;
    }

    public void setUndercharging(double undercharging) {
        this.undercharging = undercharging;
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

    public EnergyStorage_ChargeBehaviors getChargebehavior() {
        return chargebehavior;
    }

    public void setChargebehavior(EnergyStorage_ChargeBehaviors chargebehavior) {
        this.chargebehavior = chargebehavior;
    }

    public EnergyStorage_States getInitstate() {
        return initstate;
    }

    public void setInitstate(EnergyStorage_States initstate) {
        this.initstate = initstate;
    }
    //endregion

    public static EnergyStorageSpecification createDefault() {
        return new EnergyStorageSpecification();
    }
}
