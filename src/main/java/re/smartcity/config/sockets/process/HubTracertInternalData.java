package re.smartcity.config.sockets.process;

import re.smartcity.modeling.scheme.IOesHub;

public class HubTracertInternalData {
    private final IOesHub hub;
    private HubTracertValues tracert;

    public HubTracertInternalData(IOesHub hub) {
        this.hub = hub;
        this.tracert = HubTracertValues.builder()
                .hub(hub.getAddress())
                .build();
    }

    public IOesHub getHub() {
        return hub;
    }

    public HubTracertValues getTracert() {
        return tracert;
    }

    public void setTracert(HubTracertValues tracert) {
        this.tracert = tracert;
    }
}
