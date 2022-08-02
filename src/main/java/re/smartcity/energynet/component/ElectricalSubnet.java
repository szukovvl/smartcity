package re.smartcity.energynet.component;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import re.smartcity.common.resources.Messages;
import re.smartcity.energynet.*;
import re.smartcity.energynet.component.data.ElectricalSubnetSpecification;

public class ElectricalSubnet implements IComponentIdentification {

    // одинаковые для всех
    @JsonProperty(value = "id", access = JsonProperty.Access.READ_ONLY)
    private final long id = -1; // для данного типа не используется

    private String identy; // уникальный идентификатор

    @JsonProperty(value = "devaddr", access = JsonProperty.Access.READ_ONLY)
    private final byte devaddr = 0; // для данного типа не используется

    @JsonProperty(value = "componentType", access = JsonProperty.Access.READ_ONLY)
    private final SupportedTypes componenttype = SupportedTypes.LINE; // тип компонента

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
    public SupportedTypes getComponentType() { return this.componenttype; }
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

    public static ElectricalSubnet create(String identy) {
        if (identy == null || identy.trim().isEmpty()) {
            throw new IllegalArgumentException(Messages.ER_0);
        }
        ElectricalSubnet res = new ElectricalSubnet();
        res.identy = identy;
        res.setData(ElectricalSubnetSpecification.createDefault());
        return res;
    }
}
