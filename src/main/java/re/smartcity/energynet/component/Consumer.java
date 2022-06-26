package re.smartcity.energynet.component;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import re.smartcity.common.data.Forecast;
import re.smartcity.common.resources.Messages;
import re.smartcity.energynet.*;
import re.smartcity.energynet.component.data.ConsumerSpecification;

import java.time.LocalTime;

@Table("component")
public class Consumer implements IComponentIdentification, IConsumer {

    @Id
    private String identy; // уникальный идентификатор

    private final SupportedTypes componenttype = SupportedTypes.CONSUMER; // тип компонента

    private volatile ConsumerSpecification data;

    public Consumer() { }

    //region IComponentIdentification
    @Override
    public String getIdenty() { return this.identy; }

    @Override
    public SupportedTypes getComponentType() { return this.componenttype; }
    //endregion

    public ConsumerSpecification getData() {
        return data;
    }

    public void setData(ConsumerSpecification data) {
        this.data = data;
    }

    public static Consumer create(String identy, SupportedConsumers consumer) {
        if (identy == null || identy.trim().isEmpty()) {
            throw new IllegalArgumentException(Messages.ER_0);
        }
        Consumer res = new Consumer();
        res.identy = identy;
        res.setData(ConsumerSpecification.createDefault(consumer));
        return res;
    }
}
