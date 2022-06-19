package re.smartcity.energynet.component;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import re.smartcity.common.resources.Messages;
import re.smartcity.energynet.IComponentIdentification;
import re.smartcity.energynet.SupportedTypes;

@Table("testtable")
public class GenerationA implements IComponentIdentification {

    @Id
    private String identy; // уникальный идентификатор
    private SupportedTypes componenttype = SupportedTypes.GENERATOR; // тип компонента

    private Integer data = -1;

    public GenerationA() { }

    //region IComponentIdentification
    @Override
    public String getIdenty() { return this.identy; }

    @Override
    public SupportedTypes getComponentType() { return this.componenttype; }
    //endregion


    public Integer getData() {
        return data;
    }

    public void setData(Integer data) {
        this.data = data;
    }

    public static GenerationA create(String identy) {
        if (identy == null || identy.trim().isEmpty()) {
            throw new IllegalArgumentException(Messages.ER_0);
        }
        GenerationA res = new GenerationA();
        res.identy = identy;
        return res;
    }
}
