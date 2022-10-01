package re.smartcity.energynet.component.data;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class EnergyDistributorStackedValues { // !!! исключаю из характеристик
    private double electricitymeter; // потребленная электроэнергия с накоплением в кВт*ч.
    private double greenenergy_meter; // потребленная электроэнергия с накоплением, приходящая на зеленную энергетику
    private double loss_power_meter; // потери с накоплением
    private double losscosts; // затраты на потери электроэнергии в руб.
    private double topay; // общая стоимость энергии
}
