package re.smartcity.config.sockets.process;

import lombok.Data;

@Data
public class GameValues {

    private double energy = 0.0; // потребляемая мощность
    private double generation = 0.0; // генерируемые мощности
    private double reserve_generation = 0.0; // резервные генерируемые мощности
    private double carbon = 0.0; // экология
    private double credit = 0.0; // расходы
    private double debit = 0.0; // доходы

}
