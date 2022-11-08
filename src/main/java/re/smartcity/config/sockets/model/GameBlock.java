package re.smartcity.config.sockets.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Value;
import re.smartcity.modeling.scheme.OesRootHub;

@Value
@Builder
public class GameBlock {
    OesRootHub root;

    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    int[] udevices;

    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    PurchasedLot[] adevices;

    double credit_total;
}
