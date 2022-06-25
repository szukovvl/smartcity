package re.smartcity.energynet.component;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import re.smartcity.common.resources.Messages;
import re.smartcity.energynet.*;
import re.smartcity.energynet.component.data.GenerationSpecification;

@Table("component")
public class Generation implements IComponentIdentification, IGeneration {

    // одинаковые для всех
    @Id
    private String identy; // уникальный идентификатор
    private final SupportedTypes componenttype = SupportedTypes.GENERATOR;

    private volatile GenerationSpecification data;

    public Generation() { }

    //region IComponentIdentification
    @Override
    public String getIdenty() { return this.identy; }

    @Override
    public SupportedTypes getComponentType() { return this.componenttype; }
    //endregion

    public GenerationSpecification getData() {
        return data;
    }

    public void setData(GenerationSpecification data) {
        this.data = data;
    }

    public static Generation create(String identy) {
        if (identy == null || identy.trim().isEmpty()) {
            throw new IllegalArgumentException(Messages.ER_0);
        }
        Generation res = new Generation();
        res.identy = identy;
        res.setData(GenerationSpecification.createDefault());
        return res;
    }
}
