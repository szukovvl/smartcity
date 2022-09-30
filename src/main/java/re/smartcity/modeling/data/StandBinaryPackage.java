package re.smartcity.modeling.data;

import re.smartcity.energynet.IComponentIdentification;
import re.smartcity.modeling.TaskData;

public final class StandBinaryPackage {

    private final byte devaddr;
    private final Byte[] data;

    private IComponentIdentification oes;

    private TaskData task;

    private Byte[][] oesbin;

    public StandBinaryPackage(byte devaddr, Byte[] data) {
        this.devaddr = devaddr;
        this.data = data;
    }

    public byte getDevaddr() {
        return devaddr;
    }

    public Byte[] getData() {
        return data;
    }

    public Byte[][] getOesbin() {
        return oesbin;
    }

    public void setOesbin(Byte[][] oesbin) {
        this.oesbin = oesbin;
    }

    public IComponentIdentification getOes() {
        return oes;
    }

    public void setOes(IComponentIdentification oes) {
        this.oes = oes;
    }

    public TaskData getTask() {
        return task;
    }

    public void setTask(TaskData task) {
        this.task = task;
    }
}
