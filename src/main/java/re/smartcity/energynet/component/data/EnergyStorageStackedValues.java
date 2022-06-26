package re.smartcity.energynet.component.data;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class EnergyStorageStackedValues {
    private double electricitymeter; // потребленная электроэнергия с накоплением в кВт*ч.
    private double topay; // стоимость потребленной электроэнергии в руб.
    private double chargepowermeter; // потребленная электроэнергия на зарядку с накоплением в кВт*ч.
    private double chargetopay; // стоимость потребленной электроэнергии в руб. на зарядку хранилища
}
