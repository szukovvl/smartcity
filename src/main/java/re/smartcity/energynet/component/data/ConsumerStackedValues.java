package re.smartcity.energynet.component.data;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ConsumerStackedValues { // !!! исключаю из характеристик
    private double electricitymeter; // потребленная электроэнергия с накоплением в кВт*ч.
    private double greenenergy_meter; // потребленная электроэнергия с накоплением, приходящая на зеленную энергетику
    private double topay; // стоимость потребленной электроэнергии в руб.
}
