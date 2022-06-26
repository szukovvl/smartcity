package re.smartcity.energynet.component.data;

import lombok.Data;
import lombok.NoArgsConstructor;
import re.smartcity.energynet.EnergyStorage_States;
import re.smartcity.energynet.SupportedLoadIndicators;

import java.time.LocalTime;

@Data
@NoArgsConstructor
public class EnergyStorageInstantValues {
    private LocalTime timestamp; // метка времени примененных значений
    private double currentcost; // действующая стоимость
    private double currentpower; // мгновенное значения мощности потребления в МВт
    private double chargecost; // стоимость энергии для зарядки хранилища
    private double chargepower; // потребление энергии на зарядку
    private double capacity; // остаточная емкость хранилища.
    private SupportedLoadIndicators loadindicator; // индикатор нагрузки
    private EnergyStorage_States state; // текущее состояние хранилища
}
