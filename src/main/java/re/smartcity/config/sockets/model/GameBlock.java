package re.smartcity.config.sockets.model;

import lombok.Builder;
import lombok.Value;
import re.smartcity.modeling.scheme.OesRootHub;

@Value
@Builder
public class GameBlock {
    OesRootHub root;
}
