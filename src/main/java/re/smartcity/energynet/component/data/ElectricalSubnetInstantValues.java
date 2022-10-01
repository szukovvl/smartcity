package re.smartcity.energynet.component.data;

import lombok.Data;
import lombok.NoArgsConstructor;
import re.smartcity.energynet.SupportedLoadIndicators;

import java.time.LocalTime;

@Data
@NoArgsConstructor
public class ElectricalSubnetInstantValues { // !!! исключаю из характеристик
    private LocalTime timestamp; // метка времени примененных значений
    private double currentcost; // действующая стоимость
    private double in_power; // мгновенное значения мощности на входе
    private double in_greenenergy; // значение мощности на входе, приходящейся на зеленную энергетику (?)
    private double out_power; // мгновенное значения мощности на выходе
    private double out_greenenergy; // значение мощности на выходе, приходящейся на зеленную энергетику (?)
    private double losspower; // мгновенное значение мощности потерь в сети
    private SupportedLoadIndicators loadindicator; // индикатор нагрузки
}
