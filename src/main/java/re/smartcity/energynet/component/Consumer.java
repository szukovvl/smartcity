package re.smartcity.energynet.component;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import re.smartcity.common.resources.Messages;
import re.smartcity.energynet.*;
import re.smartcity.energynet.component.data.ConsumerSpecification;

@Table("component")
public class Consumer implements IComponentIdentification, IConsumer {

    @Id
    private long id;

    private String identy; // уникальный идентификатор

    private byte devaddr; // сетевой уникальный адрес устройства

    @JsonProperty(value = "componentType", access = JsonProperty.Access.READ_ONLY)
    private final SupportedTypes componentType = SupportedTypes.CONSUMER; // тип компонента

    private volatile ConsumerSpecification data;

    public Consumer() { }

    //region IComponentIdentification
    @Override
    public long getId() {
        return this.id;
    }

    @Override
    public String getIdenty() { return this.identy; }

    @Override
    public byte getDevaddr() {
        return this.devaddr;
    }

    @Override
    public SupportedTypes getComponentType() { return this.componentType; }
    //endregion

    public ConsumerSpecification getData() {
        return data;
    }

    public void setData(ConsumerSpecification data) {
        this.data = data;
    }

    public static Consumer create(String identy, byte devaddr, SupportedConsumers consumer) {
        if (identy == null || identy.trim().isEmpty()) {
            throw new IllegalArgumentException(Messages.ER_0);
        }
        if (devaddr == 0) {
            throw new IllegalArgumentException(Messages.ER_9);
        }
        Consumer res = new Consumer();
        res.identy = identy;
        res.devaddr = devaddr;
        res.setData(ConsumerSpecification.createDefault(consumer));
        return res;
    }
}
