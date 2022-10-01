package re.smartcity.modeling.scheme;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import re.smartcity.energynet.component.ElectricalSubnet;

public class SubnetHub implements IControlHub {

    @JsonIgnore
    private final ElectricalSubnet ownline;

    private final int devaddr; // адрес устройства
    private boolean off = false; // объект отключен

    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    private IControlHub[] consumers;

    public SubnetHub(ElectricalSubnet line) {
        this.ownline = line;
        this.devaddr = line.getDevaddr();
    }

    public ElectricalSubnet getOwnline() {
        return ownline;
    }

    public IControlHub[] getConsumers() {
        return consumers;
    }

    public void setConsumers(IControlHub[] consumers) {
        this.consumers = consumers;
    }

    public boolean hasChilds() {
        return this.consumers != null && this.consumers.length != 0;
    }

    //region IControlHub
    @Override
    public int getDevaddr() {
        return devaddr;
    }

    @Override
    public boolean isOff() {
        return off;
    }

    @Override
    public void setOff(boolean off) {
        this.off = off;
    }
    //endregion

}
