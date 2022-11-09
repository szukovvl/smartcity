package re.smartcity.config.sockets.process;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class HubTracertValues {

    @Builder.Default
    GameValues values = GameValues.builder().build();

    @Builder.Default
    GameValues totals = GameValues.builder().build();

    @Builder.Default
    int hub = 0;

}
