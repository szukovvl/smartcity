package re.smartcity.config.sockets.model;

public class ResponseScenesDataBuilder {

    private ResponseScenesEventData data = new ResponseScenesEventData();

    public ResponseScenesDataBuilder (int key) {
        this.data.setMainstation(key);
    }

    public ResponseScenesEventData build() {
        return data;
    }

    public ResponseScenesDataBuilder substation(int key) {
        data.setSubstation(key);
        return this;
    }

    public ResponseScenesDataBuilder consumers(int[] keys) {
        data.setConsumers(keys);
        return this;
    }

    public ResponseScenesDataBuilder sceneIdentify(SceneIdentifyData sceneData) {
        data.setSceneidentify(sceneData);
        return this;
    }
}
