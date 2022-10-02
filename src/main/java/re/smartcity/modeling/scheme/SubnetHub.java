package re.smartcity.modeling.scheme;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import re.smartcity.energynet.IComponentIdentification;
import re.smartcity.energynet.component.ElectricalSubnet;

public class SubnetHub implements IControlHub {

    @JsonIgnore
    private final ElectricalSubnet ownline;

    private final int devaddr; // адрес устройства
    private final IComponentIdentification linkedOes;
    private boolean off = false; // объект отключен
    private String error;

    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    private IControlHub[] items;

    public SubnetHub(ElectricalSubnet line, IComponentIdentification oes) {
        this.ownline = line;
        this.devaddr = line != null ? line.getDevaddr() : oes.getDevaddr();
        this.linkedOes = oes;
    }

    public ElectricalSubnet getOwnline() {
        return ownline;
    }

    public IControlHub[] getItems() {
        return this.items;
    }

    public void setItems(IControlHub[] items) {
        this.items = items;
    }

    public boolean hasChilds() {
        return this.items != null && this.items.length != 0;
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
