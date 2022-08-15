package re.smartcity.energynet.component.data.client;

import lombok.Data;
import lombok.NoArgsConstructor;
import re.smartcity.common.resources.Messages;
import re.smartcity.energynet.component.data.MainSubstationSpecification;

@Data
@NoArgsConstructor
public final class SmallMainStationSpecification {

    private volatile SmallSubnetSpecification[] inputs; // подсети ввода энергии только генерация
    private volatile SmallSubnetSpecification[] outputs; // подсети потребителей только потребление
    private volatile double external_energy = 0; // внешний стабильный источник энергии
    private volatile double carbon = 684.75; // г/кВт*ч только для внешней энергии

    public static void validate(SmallMainStationSpecification data) {
        if (data.getExternal_energy() < 0.0) {
            throw new IllegalArgumentException(Messages.ER_1);
        }

        for (int i = 0; i < data.getInputs().length; i++) {
            SmallSubnetSpecification.validate(data.getInputs()[i]);
        }
        for (int i = 0; i < data.getOutputs().length; i++) {
            SmallSubnetSpecification.validate(data.getOutputs()[i]);
        }
    }

    public static void AssignTo(SmallMainStationSpecification src, MainSubstationSpecification dest) {
        dest.setExternal_energy(src.getExternal_energy());
        dest.setCarbon(src.getCarbon());

        for (int i = 0; i < src.getInputs().length; i++) {
            SmallSubnetSpecification.AssignTo(src.getInputs()[i], dest.getInputs()[i].getData());
        }
        for (int i = 0; i < src.getOutputs().length; i++) {
            SmallSubnetSpecification.AssignTo(src.getOutputs()[i], dest.getOutputs()[i].getData());
        }
    }
}
