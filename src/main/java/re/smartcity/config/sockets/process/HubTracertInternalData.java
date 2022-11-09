package re.smartcity.config.sockets.process;

import re.smartcity.energynet.IComponentIdentification;
import re.smartcity.modeling.scheme.IOesHub;

public class HubTracertInternalData {
    private final IOesHub hub;
    private final IComponentIdentification oes;
    private HubTracertValues tracert;

    public HubTracertInternalData(IOesHub hub, IComponentIdentification oes) {
        this.hub = hub;
        this.oes = oes;
        this.tracert = HubTracertValues.builder()
                .hub(hub.getAddress())
                .build();
    }

    public IComponentIdentification getOes() {
        return oes;
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
