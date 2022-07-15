package re.smartcity.energynet.component.data.client;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;
import re.smartcity.common.data.Forecast;
import re.smartcity.common.resources.Messages;
import re.smartcity.energynet.GenerationUsageModes;
import re.smartcity.energynet.component.data.GenerationSpecification;

@Data
@NoArgsConstructor
public final class SmallGenerationSpecification {

    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    private Forecast forecast; // прогноз

    private boolean useforecast; // задействовать прогноз

    private double energy; // максимальная мощность в МВт

    private double carbon = 0.0; // выброс CO2 (экология)

    private double highload = 0.8; // значение в процента от генерируемой мощности, высокая нагрузка

    private double criticalload = 0.9; // значение в процентах от генерируемой мощности, критическая нагрузка

    private int blackouttime = 300; // время в секундах, прежде чем произойдет отключение генерации

    private double tariff = 0.0; // тариф

    private GenerationUsageModes mode = GenerationUsageModes.RESERVE; // режим использования

    public static void validate(SmallGenerationSpecification data) {
        if (data.getEnergy() < 0.0) {
            throw new IllegalArgumentException(Messages.ER_1);
        }
        if (data.isUseforecast() && data.getForecast() == null) {
            throw new IllegalArgumentException(Messages.ER_2);
        }
        if (data.getCarbon() < 0.0) {
            throw new IllegalArgumentException(Messages.ER_4);
        }

        if (data.getHighload() < 0.0 || data.getCriticalload() < 0.0) {
            throw new IllegalArgumentException(Messages.ER_5);
        }
        if (data.getBlackouttime() < 0) {
            throw new IllegalArgumentException(Messages.ER_6);
        }
        if (data.getTariff() < 0.0) {
            throw new IllegalArgumentException(Messages.ER_7);
        }
    }

    public static void AssignTo(SmallGenerationSpecification src, GenerationSpecification dest) {
        dest.setEnergy(src.getEnergy());
        dest.setUseforecast(src.isUseforecast());
        dest.setForecast(src.getForecast());
        dest.setCarbon(src.getCarbon());
        dest.setHighload(src.getHighload());
        dest.setCriticalload(src.getCriticalload());
        dest.setBlackouttime(src.getBlackouttime());
        dest.setTariff(src.getTariff());
        dest.setMode(src.getMode());
    }
}
