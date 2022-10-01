package re.smartcity.energynet.component.data;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class GenerationStackedValues { // !!! исключаю из характеристик
    private double electricitymeter; // потребленная электроэнергия с накоплением в кВт*ч.
    private double topay; // стоимость потребленной электроэнергии в руб.
    private double unusedmeter; // счетчик невостребованной энергии
    private double lostprofit; // упущенная выгода
    private double totalcarbon; // итоговое значения загрязнения
}
