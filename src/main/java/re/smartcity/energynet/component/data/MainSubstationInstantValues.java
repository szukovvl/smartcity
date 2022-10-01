package re.smartcity.energynet.component.data;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Data
@NoArgsConstructor
public class MainSubstationInstantValues { // !!! исключаю из характеристик
    private LocalTime timestamp; // метка времени примененных значений
    private double currentpower; // мгновенное значения мощности потребления в МВт
    private double current_greenenergy; // значение мощности, приходящейся на зеленную энергетику (?)
    private double losspower; // потери в сетях
    private double extpower; // потребление из ВИЭ
    private double currentcarbon; // мгновенное значения загрязнения
}
