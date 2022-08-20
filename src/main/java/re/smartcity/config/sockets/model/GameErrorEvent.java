package re.smartcity.config.sockets.model;

import re.smartcity.config.sockets.GameClientEvent;

public record GameErrorEvent(String clientData, String errorMessage) {
}
