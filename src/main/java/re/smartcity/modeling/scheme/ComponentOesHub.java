package re.smartcity.modeling.scheme;

import re.smartcity.common.resources.Messages;
import re.smartcity.energynet.IComponentIdentification;

public class ComponentOesHub implements IControlHub {

    private final IComponentIdentification linkedOes; // может быть нулем, если подключен неизвестный объект (?)
    private final int devaddr; // адрес устройства
    private boolean off = false; // объект отключен
    private String error;

    public ComponentOesHub(IComponentIdentification linkedOes, byte linkaddr) {
        this.linkedOes = linkedOes;
        this.devaddr = linkaddr;
    }

    //region IControlHub
    @Override
    public int getDevaddr() {
        return devaddr;
    }

    @Override
    public IComponentIdentification getLinkedOes() { return this.linkedOes; }

    @Override
    public boolean isOff() {
        return off;
    }

    @Override
    public void setOff(boolean off) {
        this.off = off;
    }

    @Override
    public String getErrorMsg() { return this.error; }

    @Override
    public void setErrorMsg(String msg) { this.error = msg; }
    //endregion
}
