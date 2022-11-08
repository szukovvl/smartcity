package re.smartcity.config.sockets.process;

import lombok.Builder;
import lombok.Value;
import re.smartcity.modeling.scheme.OesRootHub;

@Value
@Builder
public class GameValues {

    @Builder.Default
    double energy = 0.0; // потребляемая мощность

    @Builder.Default
    double generation = 0.0; // генерируемые мощности

    @Builder.Default
    double carbon = 0.0; // экология

    @Builder.Default
    double credit = 0.0; // расходы

    @Builder.Default
    double debit = 0.0; // доходы

}
