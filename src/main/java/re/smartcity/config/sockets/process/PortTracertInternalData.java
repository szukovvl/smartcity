package re.smartcity.config.sockets.process;

import re.smartcity.modeling.scheme.IConnectionPort;

public final class PortTracertInternalData {
    private final IConnectionPort port;
    private PortTracertValues tracert;

    public PortTracertInternalData(IConnectionPort port) {
        this(port, 0);
    }

    public PortTracertInternalData(IConnectionPort port, int owner) {
        this.port = port;
        this.tracert = new PortTracertValues(port.getAddress(), owner);
        this.tracert.setOn(port.isOn());
    }

    public IConnectionPort getPort() {
        return port;
    }

    public PortTracertValues getTracert() {
        return tracert;
    }

    public void setTracert(PortTracertValues tracert) {
        this.tracert = tracert;
    }
}
