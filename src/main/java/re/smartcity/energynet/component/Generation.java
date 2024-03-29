package re.smartcity.energynet.component;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import re.smartcity.common.resources.Messages;
import re.smartcity.energynet.*;
import re.smartcity.energynet.component.data.GenerationSpecification;

@Table("component")
public class Generation implements IComponentIdentification, IGeneration {

    // одинаковые для всех
    @JsonProperty(value = "id", access = JsonProperty.Access.READ_ONLY)
    @Id
    private long id;

    @JsonProperty(value = "identy", access = JsonProperty.Access.READ_ONLY)
    private String identy; // уникальный идентификатор

    @JsonProperty(value = "devaddr", access = JsonProperty.Access.READ_ONLY)
    private byte devaddr; // сетевой уникальный адрес устройства

    @JsonProperty(value = "componentType", access = JsonProperty.Access.READ_ONLY)
    private final SupportedTypes componentType = SupportedTypes.GENERATOR;

    private volatile GenerationSpecification data;

    public Generation() { }

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

    public GenerationSpecification getData() {
        return data;
    }

    public void setData(GenerationSpecification data) {
        this.data = data;
    }

    public static Generation create(String identy, byte devaddr) {
        if (identy == null || identy.trim().isEmpty()) {
            throw new IllegalArgumentException(Messages.ER_0);
        }
        if (devaddr == 0) {
            throw new IllegalArgumentException(Messages.ER_9);
        }
        Generation res = new Generation();
        res.identy = identy;
        res.devaddr = devaddr;
        res.setData(GenerationSpecification.createDefault());
        return res;
    }
}
