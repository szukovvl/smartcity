package re.smartcity.config.sockets.process;

import lombok.Data;

@Data
public class PortTracertValues {

    private GameValues values = new GameValues();

    private GameValues totals = new GameValues();

    private final int port;

    private final int owner;

    private int game_step = 0;

    private boolean on = false;

    private PortStates state = PortStates.LOW;

    private AppliedTariffZones zone = AppliedTariffZones.NONE_DAY_PEAK;

    public PortTracertValues(int port, int owner) {
        this.port = port;
        this.owner = owner;
    }

}
