package re.smartcity.energynet.component.data;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ElectricalSubnetStackedValues {
    private double in_powermeter; // мощность в кВт*ч на входе.
    private double in_greenpower_meter; // мощность, приходящая на зеленную энергетику, на входе
    private double out_powermeter; // мощность в кВт*ч, на выходе.
    private double out_greenpower_meter; // мощность, приходящая на зеленную энергетику, на выходе
    private double loss_power_meter; // потери с накоплением
    private double losscosts; // затраты на потери электроэнергии в руб.
    private double topay; // оплата за потребленную энергию
}
