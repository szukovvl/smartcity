package re.smartcity.modeling.data;

import com.fasterxml.jackson.annotation.JsonInclude;
import re.smartcity.config.sockets.model.SceneIdentifyData;
import re.smartcity.energynet.component.Consumer;
import re.smartcity.energynet.component.EnergyDistributor;

public class GamerScenesData {

    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    private Consumer[] predefconsumers = new Consumer[0];

    private final EnergyDistributor substation;

    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    private SceneIdentifyData sceneIdentify;

    public GamerScenesData (EnergyDistributor substation) {
        this.substation = substation;
    }

    public Consumer[] getPredefconsumers() {
        return predefconsumers;
    }

    public void setPredefconsumers(Consumer[] predefconsumers) {
        this.predefconsumers = predefconsumers;
    }

    public EnergyDistributor getSubstation() {
        return substation;
    }

    public SceneIdentifyData getSceneIdentify() {
        return sceneIdentify;
    }

    public void setSceneIdentify(SceneIdentifyData sceneIdentify) {
        this.sceneIdentify = sceneIdentify;
    }
}