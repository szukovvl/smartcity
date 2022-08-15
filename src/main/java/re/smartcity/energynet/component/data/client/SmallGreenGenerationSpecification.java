package re.smartcity.energynet.component.data.client;

import lombok.Data;
import lombok.NoArgsConstructor;
import re.smartcity.common.resources.Messages;
import re.smartcity.energynet.GenerationUsageModes;
import re.smartcity.energynet.component.data.GreenGenerationSpecification;

@Data
@NoArgsConstructor
public class SmallGreenGenerationSpecification {

    private double energy = 0.1; // максимальная мощность в МВт
    private double highload = 0.8; // значение в процента от генерируемой мощности, высокая нагрузка
    private double criticalload = 0.95; // значение в процентах от генерируемой мощности, критическая нагрузка
    private int blackouttime = 300; // время в секундах, прежде чем произойдет отключение генерации
    private double carbon = 700.0; // г/кВт*ч
    private GenerationUsageModes mode = GenerationUsageModes.ALWAYS; // режим использования

    public static void validate(SmallGreenGenerationSpecification data) {
        if (data.getEnergy() < 0.0) {
            throw new IllegalArgumentException(Messages.ER_1);
        }
        if (data.getCarbon() < 0.0) {
            throw new IllegalArgumentException(Messages.ER_4);
        }

        if (data.getBlackouttime() < 0) {
            throw new IllegalArgumentException(Messages.ER_6);
        }
    }

    public static void AssignTo(SmallGreenGenerationSpecification src, GreenGenerationSpecification dest) {
        dest.setEnergy(src.getEnergy());
        dest.setCarbon(src.getCarbon());
        dest.setHighload(src.getHighload());
        dest.setCriticalload(src.getCriticalload());
        dest.setBlackouttime(src.getBlackouttime());
        dest.setMode(src.getMode());
    }
}
