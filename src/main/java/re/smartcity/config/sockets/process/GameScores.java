package re.smartcity.config.sockets.process;

import lombok.Data;

@Data
public class GameScores {
    private double scores = 0.0; // баллы игрока
    private double balance = 0.0; // баланс мощности
    private double economic = 0.0; // экономическая эффективность
    private double ecology = 0.0; // экология
}
