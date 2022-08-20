package re.smartcity.config.sockets.model;

import re.smartcity.modeling.GameStatuses;

public record GameStatusEvent(GameStatuses status, boolean administration, int gamers, int guests) {
}
