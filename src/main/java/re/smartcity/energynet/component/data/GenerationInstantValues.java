package re.smartcity.energynet.component.data;

import lombok.Data;
import lombok.NoArgsConstructor;
import re.smartcity.energynet.SupportedLoadIndicators;

import java.time.LocalTime;

@Data
@NoArgsConstructor
public class GenerationInstantValues { // !!! исключаю из характеристик
    private LocalTime timestamp; // метка времени примененных значений
    private double currentcost; // действующая тариф
    private double currentpower; // мгновенное значения мощности потребления в МВт
    private double currentgeneration; // мгновенное значение генерации
    private double unusedenergy; // незадействованные мощности
    private double currentcarbon; // мгновенное значения загрязнения
    private SupportedLoadIndicators loadindicator; // индикатор нагрузки
}
