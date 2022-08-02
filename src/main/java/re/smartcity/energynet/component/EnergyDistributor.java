package re.smartcity.energynet.component;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import re.smartcity.common.resources.Messages;
import re.smartcity.energynet.*;
import re.smartcity.energynet.component.data.EnergyDistributorSpecification;

@Table("component")
public class EnergyDistributor implements IComponentIdentification {

    @JsonProperty(value = "id", access = JsonProperty.Access.READ_ONLY)
    @Id
    private long id;

    @JsonProperty(value = "identy", access = JsonProperty.Access.READ_ONLY)
    private String identy; // уникальный идентификатор

    @JsonProperty(value = "devaddr", access = JsonProperty.Access.READ_ONLY)
    private byte devaddr; // сетевой уникальный адрес устройства

    @JsonProperty(value = "componenttype", access = JsonProperty.Access.READ_ONLY)
    private final SupportedTypes componenttype = SupportedTypes.DISTRIBUTOR;

    private volatile EnergyDistributorSpecification data;

    public EnergyDistributor() { }

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

    public EnergyDistributorSpecification getData() {
        return data;
    }

    public void setData(EnergyDistributorSpecification data) {
        this.data = data;
    }

    public static EnergyDistributor create(String identy, byte devaddr) {
        if (identy == null || identy.trim().isEmpty()) {
            throw new IllegalArgumentException(Messages.ER_0);
        }
        if (devaddr == 0) {
            throw new IllegalArgumentException(Messages.ER_9);
        }
        EnergyDistributor res = new EnergyDistributor();
        res.identy = identy;
        res.devaddr = devaddr;
        res.setData(EnergyDistributorSpecification.createDefault(identy));
        return res;
    }
}
