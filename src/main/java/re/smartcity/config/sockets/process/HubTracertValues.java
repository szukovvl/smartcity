package re.smartcity.config.sockets.process;

import lombok.Data;

@Data
public class HubTracertValues {

    private GameValues values = new GameValues();
    private GameValues totals = new GameValues();
    private GeneratorStatuses genstatus = GeneratorStatuses.NONE;
    private final int hub;

    public HubTracertValues(int address) {
        this.hub = address;
    }
}
