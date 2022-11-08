package re.smartcity.config.sockets.process;

import lombok.Data;

@Data
public class GameDataset {
    private final int key;
    private GameValues cumulative_total = GameValues.builder().build(); // значения с накоплением
    private GameValues instant_values = GameValues.builder().build(); // мгновенные значения
    private int seconds = 0;

    public GameDataset(int key) {
        this.key = key;
    }
}
