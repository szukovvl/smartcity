package re.smartcity.config.sockets.process;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class PortTracertValues {

    @Builder.Default
    GameValues values = GameValues.builder().build();

    @Builder.Default
    GameValues totals = GameValues.builder().build();

    @Builder.Default
    int port = 0;

    @Builder.Default
    int owner = 0;

    @Builder.Default
    int game_step = 0;

    @Builder.Default
    boolean on = false;

    @Builder.Default
    PortStates state = PortStates.LOW;

    @Builder.Default
    AppliedTariffZones zone = AppliedTariffZones.NONE_DAY_PEAK;

}
