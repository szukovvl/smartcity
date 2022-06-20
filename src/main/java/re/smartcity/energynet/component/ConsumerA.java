package re.smartcity.energynet.component;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import re.smartcity.common.resources.Messages;
import re.smartcity.energynet.*;

@Table("testtable")
public class ConsumerA implements IComponentIdentification {

    @Id
    private String identy; // уникальный идентификатор
    private SupportedTypes componenttype = SupportedTypes.CONSUMER; // тип компонента

    private DataA data = new DataA();

    public ConsumerA() { }

    //region IComponentIdentification
    @Override
    public String getIdenty() { return this.identy; }

    @Override
    public SupportedTypes getComponentType() { return this.componenttype; }
    //endregion


    public DataA getData() {
        return data;
    }

    public void setData(DataA data) {
        this.data = data;
    }

    public static ConsumerA create(String identy) {
        if (identy == null || identy.trim().isEmpty()) {
            throw new IllegalArgumentException(Messages.ER_0);
        }
        ConsumerA res = new ConsumerA();
        res.identy = identy;
        return res;
    }
}
