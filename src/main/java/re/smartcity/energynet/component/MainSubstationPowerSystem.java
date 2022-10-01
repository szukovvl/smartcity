package re.smartcity.energynet.component;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import re.smartcity.common.resources.Messages;
import re.smartcity.energynet.IComponentIdentification;
import re.smartcity.energynet.SupportedTypes;
import re.smartcity.energynet.component.data.MainSubstationSpecification;

@Table("component")
public class MainSubstationPowerSystem implements IComponentIdentification {

    @JsonProperty(value = "id", access = JsonProperty.Access.READ_ONLY)
    @Id
    private long id;

    @JsonProperty(value = "identy", access = JsonProperty.Access.READ_ONLY)
    private String identy; // уникальный идентификатор

    @JsonProperty(value = "devaddr", access = JsonProperty.Access.READ_ONLY)
    private byte devaddr; // сетевой уникальный адрес устройства

    @JsonProperty(value = "componentType", access = JsonProperty.Access.READ_ONLY)
    private final SupportedTypes componentType = SupportedTypes.MAINSUBSTATION; // тип компонента

    private volatile MainSubstationSpecification data;

    public MainSubstationPowerSystem() { }

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

    public MainSubstationSpecification getData() {
        return data;
    }

    public void setData(MainSubstationSpecification data) {
        this.data = data;
    }

    public static MainSubstationPowerSystem create(String identy, byte devaddr, byte[] inputs, byte[] outputs) {
        if (identy == null || identy.trim().isEmpty()) {
            throw new IllegalArgumentException(Messages.ER_0);
        }
        if (devaddr == 0) {
            throw new IllegalArgumentException(Messages.ER_9);
        }
        MainSubstationPowerSystem res = new MainSubstationPowerSystem();
        res.identy = identy;
        res.devaddr = devaddr;
        res.setData(MainSubstationSpecification.createDefault(identy, inputs, outputs));
        return res;
    }
}
