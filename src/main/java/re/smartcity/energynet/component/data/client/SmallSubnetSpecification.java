package re.smartcity.energynet.component.data.client;

import lombok.Data;
import lombok.NoArgsConstructor;
import re.smartcity.common.resources.Messages;
import re.smartcity.energynet.component.data.ElectricalSubnetSpecification;

@Data
@NoArgsConstructor
public class SmallSubnetSpecification {

    private double energy = 1.0; // максимальная мощность в МВт
    private volatile double lossfactor = 0.9; // потери в сети (устанавливается при первой инициализации)
    private volatile double highload = 0.8; // значение в процента от мощности, высокая нагрузка (80%)
    private volatile double criticalload = 0.95; // значение в процентах от мощности, критическая нагрузка (95)
    private volatile int blackouttime = 300; // время в секундах, прежде чем произойдет отключение
    private volatile double tariff = 0.0; // (? котловой) (во время работы - задают администратор)

    public static void validate(SmallSubnetSpecification data) {
        if (data.getEnergy() < 0.0) {
            throw new IllegalArgumentException(Messages.ER_1);
        }

        if (data.getBlackouttime() < 0) {
            throw new IllegalArgumentException(Messages.ER_6);
        }
        if (data.getTariff() < 0.0) {
            throw new IllegalArgumentException(Messages.ER_7);
        }
    }

    public static void AssignTo(SmallSubnetSpecification src, ElectricalSubnetSpecification dest) {
        dest.setEnergy(src.getEnergy());
        dest.setLossfactor(src.getLossfactor());
        dest.setHighload(src.getHighload());
        dest.setCriticalload(src.getCriticalload());
        dest.setBlackouttime(src.getBlackouttime());
        dest.setTariff(src.getTariff());
    }
}
