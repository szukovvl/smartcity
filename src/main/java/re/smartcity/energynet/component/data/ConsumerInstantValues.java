package re.smartcity.energynet.component.data;

import lombok.Data;
import lombok.NoArgsConstructor;
import re.smartcity.energynet.SupportedAppliedZone;
import re.smartcity.energynet.SupportedLoadIndicators;

import java.time.LocalTime;

@Data
@NoArgsConstructor
public class ConsumerInstantValues { // !!! исключаю из характеристик
    private LocalTime timestamp; // метка времени примененных значений
    private double currentcost; // действующая стоимость
    private double currentpower; // мгновенное значения мощности потребления в МВт
    private double current_greenenergy; // значение мощности, приходящейся на зеленную энергетику (?)
    private SupportedLoadIndicators loadindicator; // индикатор нагрузки
    private SupportedAppliedZone appliedZone; // действующая зона тарифа при определении стоимости
}
