package re.smartcity.energynet.component;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import re.smartcity.common.resources.Messages;
import re.smartcity.energynet.*;
import re.smartcity.energynet.component.data.EnergyStorageSpecification;

import java.time.LocalTime;

@Table("component")
public class EnergyStorage implements IComponentIdentification, IEnergyStorage {

    @JsonProperty(value = "id", access = JsonProperty.Access.READ_ONLY)
    @Id
    private long id;

    @JsonProperty(value = "identy", access = JsonProperty.Access.READ_ONLY)
    private String identy; // уникальный идентификатор

    @JsonProperty(value = "devaddr", access = JsonProperty.Access.READ_ONLY)
    private byte devaddr; // сетевой уникальный адрес устройства

    @JsonProperty(value = "componentType", access = JsonProperty.Access.READ_ONLY)
    private final SupportedTypes componentType = SupportedTypes.STORAGE; // тип компонента

    private volatile EnergyStorageSpecification data;

    public EnergyStorage() { }

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
    //endregion

    public EnergyStorageSpecification getData() {
        return data;
    }

    public void setData(EnergyStorageSpecification data) {
        this.data = data;
    }

    public static EnergyStorage create(String identy, byte devaddr) {
        if (identy == null || identy.trim().isEmpty()) {
            throw new IllegalArgumentException(Messages.ER_0);
        }
        if (devaddr == 0) {
            throw new IllegalArgumentException(Messages.ER_9);
        }
        EnergyStorage res = new EnergyStorage();
        res.devaddr = devaddr;
        res.identy = identy;
        res.setData(EnergyStorageSpecification.createDefault());
        return res;
    }
}
