package re.smartcity.energynet.component.data;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Data
@NoArgsConstructor
public class EnergyDistribotorInstantValues {
    private LocalTime timestamp; // метка времени примененных значений
    private double currentpower; // мгновенное значения мощности потребления в МВт
    private double current_greenenergy; // значение мощности, приходящейся на зеленную энергетику (?)
    private double losspower; // потери в сетях
}
