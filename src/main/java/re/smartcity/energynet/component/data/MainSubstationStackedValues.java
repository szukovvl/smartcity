package re.smartcity.energynet.component.data;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class MainSubstationStackedValues {
    private double electricitymeter; // потребленная электроэнергия с накоплением в кВт*ч.
    private double greenenergy_meter; // потребленная электроэнергия с накоплением, приходящая на зеленную энергетику
    private double loss_power_meter; // потери с накоплением
    private double losscosts; // затраты на потери электроэнергии в руб.
    private double topay; // общая стоимость энергии
    private double extpower_meter; // потребленная электроэнергия с ВИЭ
    private double ext_topay; // оплата за ВИЭ
    private double totalcarbon; // итоговое значения загрязнения
}