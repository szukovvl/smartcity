package re.smartcity.config.sockets.process;

import lombok.Data;

@Data
public class GameDataset {
    private final int key;
    private GameValues cumulative_total = new GameValues(); // значения с накоплением
    private GameValues instant_values = new GameValues(); // мгновенные значения
    private int seconds = 0;

    private HubTracertValues root_values;

    private PortTracertValues[] port_values = new PortTracertValues[0];
    private HubTracertValues[] hub_values = new HubTracertValues[0];

    private int windpower = 0;
    private int sunpower = 0;

    public GameDataset(int key) {
        this.key = key;
    }
}
