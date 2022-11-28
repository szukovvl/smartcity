package re.smartcity.config.sockets.process;

import re.smartcity.energynet.GenerationUsageModes;
import re.smartcity.energynet.IComponentIdentification;
import re.smartcity.energynet.component.EnergyStorage;
import re.smartcity.energynet.component.Generation;
import re.smartcity.energynet.component.GreenGeneration;
import re.smartcity.modeling.scheme.IOesHub;

public class HubTracertInternalData {
    private final IOesHub hub;
    private final IComponentIdentification oes;
    private final EnergyStorageModel storageModel;
    private HubTracertValues tracert;
    private double[] forecast = null;
    private byte illumination = 0;
    private int internal_timer = 0;

    public HubTracertInternalData(IOesHub hub, IComponentIdentification oes) {
        this.hub = hub;
        this.oes = oes;
        this.tracert = new HubTracertValues(hub.getAddress());
        this.storageModel = EnergyStorageModel.create(oes);
        initGenerationStatus(oes);
    }

    public void initGenerationStatus(IComponentIdentification oes) {
        switch (oes.getComponentType()) {
            case GENERATOR -> this.tracert.setGenstatus(
                    ((Generation) oes).getData().getMode() == GenerationUsageModes.ALWAYS
                            ? GeneratorStatuses.ACTIVE
                            : GeneratorStatuses.RESERVED
            );
            case GREEGENERATOR -> this.tracert.setGenstatus(
                    ((GreenGeneration) oes).getData().getMode() == GenerationUsageModes.ALWAYS
                            ? GeneratorStatuses.ACTIVE
                            : GeneratorStatuses.RESERVED
            );
            case STORAGE -> this.tracert.setGenstatus(
                    ((EnergyStorage) oes).getData().getMode() == GenerationUsageModes.ALWAYS
                            ? GeneratorStatuses.ACTIVE
                            : GeneratorStatuses.RESERVED
            );
        }
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

    public EnergyStorageModel getStorageModel() {
        return storageModel;
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

    public int getInternal_timer() {
        return internal_timer;
    }

    public void setInternal_timer(int internal_timer) {
        this.internal_timer = internal_timer;
    }
}
