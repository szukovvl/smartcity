package re.smartcity.energynet.component;

import com.fasterxml.jackson.annotation.JsonProperty;
import re.smartcity.common.resources.Messages;
import re.smartcity.energynet.*;
import re.smartcity.energynet.component.data.ElectricalSubnetSpecification;

public class ElectricalSubnet implements IComponentIdentification {

    // одинаковые для всех
    @JsonProperty(value = "id", access = JsonProperty.Access.READ_ONLY)
    private final long id = -1; // для данного типа не используется

    private String identy; // уникальный идентификатор

    private byte devaddr; // сетевой уникальный адрес устройства

    @JsonProperty(value = "componentType", access = JsonProperty.Access.READ_ONLY)
    private final SupportedTypes componentType = SupportedTypes.LINE; // тип компонента

    private volatile ElectricalSubnetSpecification data;

    public ElectricalSubnet() { }

    //region IComponentIdentification
    @Override
    public long getId() {
        return id;
    }

    @Override
    public String getIdenty() { return this.identy; }

    @Override
    public byte getDevaddr() {
        return devaddr;
    }

    @Override
    public SupportedTypes getComponentType() { return this.componentType; }

    @Override
    public boolean itIsMine(int address) {
        return this.devaddr == address;
    }
    //endregion

    public void setIdenty(String identy) {
        this.identy = identy;
    }

    public ElectricalSubnetSpecification getData() {
        return data;
    }

    public void setData(ElectricalSubnetSpecification data) {
        this.data = data;
    }

    public static ElectricalSubnet create(String identy, byte devaddr) {
        if (identy == null || identy.trim().isEmpty()) {
            throw new IllegalArgumentException(Messages.ER_0);
        }
        ElectricalSubnet res = new ElectricalSubnet();
        res.devaddr = devaddr;
        res.identy = identy;
        res.setData(ElectricalSubnetSpecification.createDefault());
        return res;
    }
}
