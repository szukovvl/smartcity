package re.smartcity.energynet.component.data.client;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;
import re.smartcity.common.data.Forecast;
import re.smartcity.common.resources.Messages;
import re.smartcity.energynet.EnergyStorage_ChargeBehaviors;
import re.smartcity.energynet.EnergyStorage_States;
import re.smartcity.energynet.GenerationUsageModes;
import re.smartcity.energynet.component.data.EnergyStorageSpecification;

@Data
@NoArgsConstructor
public final class SmallStorageSpecification {

    private double energy; // максимальная мощность в МВт

    private double carbon = 798.7; // выброс CO2 (экология)

    private int blackouttime = 300; // время в секундах, прежде чем произойдет отключение генерации

    private double tariff = 0.0; // тариф

    private double performance = 0.88; // показатель эффективности системы хранения

    private double peckertexponent = 1.1; // экспонента Пекерта

    private double outpower = 0.5; // граница нормального значения мощности, отдаваемой потребителю, в процентах

    private boolean overload_enabled = false; // разрешение превышения установленного параметра границы нормального значения отдаваемой мощности

    private double maxdischarge = 0.2; // максимальная разрядка хранилища, в процентах

    private double undercharging = 0.7; // недозарядка, когда устройство может быть вновь использовано

    private double criticalload = 0.9; // критическое значение нагрузки на хранилище

    private GenerationUsageModes mode = GenerationUsageModes.RESERVE; // режим использования

    private EnergyStorage_ChargeBehaviors chargebehavior = EnergyStorage_ChargeBehaviors.LOWTARIFF; // поведение хранилища при восстановлении

    private EnergyStorage_States initstate = EnergyStorage_States.CHARGED; // начальное состояние перед началом игрового процесса

    public static void validate(SmallStorageSpecification data) {
        if (data.getEnergy() < 0.0) {
            throw new IllegalArgumentException(Messages.ER_1);
        }
        if (data.getCarbon() < 0.0) {
            throw new IllegalArgumentException(Messages.ER_4);
        }

        if (data.getBlackouttime() < 0) {
            throw new IllegalArgumentException(Messages.ER_6);
        }
        if (data.getTariff() < 0.0) {
            throw new IllegalArgumentException(Messages.ER_7);
        }
    }

    public static void AssignTo(SmallStorageSpecification src, EnergyStorageSpecification dest) {
        dest.setEnergy(src.getEnergy());
        dest.setCarbon(src.getCarbon());
        dest.setCriticalload(src.getCriticalload());
        dest.setBlackouttime(src.getBlackouttime());
        dest.setTariff(src.getTariff());
        dest.setPerformance(src.getPerformance());
        dest.setPeckertexponent(src.getPeckertexponent());
        dest.setOutpower(src.getOutpower());
        dest.setOverload_enabled(src.isOverload_enabled());
        dest.setMaxdischarge(src.getMaxdischarge());
        dest.setUndercharging(src.getUndercharging());
        dest.setMode(src.getMode());
        dest.setChargebehavior(src.getChargebehavior());
        dest.setInitstate(src.getInitstate());
    }
}
