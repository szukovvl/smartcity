package re.smartcity.config.sockets.process;

import re.smartcity.energynet.IComponentIdentification;
import re.smartcity.modeling.scheme.IOesHub;

public class HubTracertInternalData {
    private final IOesHub hub;
    private final IComponentIdentification oes;
    private HubTracertValues tracert;
    private double[] forecast = null;
    private byte illumination = 0;

    public HubTracertInternalData(IOesHub hub, IComponentIdentification oes) {
        this.hub = hub;
        this.oes = oes;
        this.tracert = new HubTracertValues(hub.getAddress());
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

    public double[] getForecast() {
        return forecast;
    }

    public void setForecast(double[] forecast) {
        this.forecast = forecast;
    }

    public boolean useForecast() { return this.forecast != null; }

    public byte getIllumination() {
        return illumination;
    }

    public void setIllumination(byte illumination) {
        this.illumination = illumination;
    }
}
