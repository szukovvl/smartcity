package re.smartcity.energynet.component;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import re.smartcity.common.resources.Messages;
import re.smartcity.energynet.*;
import re.smartcity.energynet.component.data.GreenGenerationSpecification;

import java.time.LocalTime;

@Table("component")
public class GreenGeneration implements IComponentIdentification {

    // одинаковые для всех
    @Id
    private String identy; // уникальный идентификатор
    private final SupportedTypes componenttype = SupportedTypes.GREEGENERATOR;

    private volatile GreenGenerationSpecification data;

    public GreenGeneration() { }

    //region IComponentIdentification
    @Override
    public String getIdenty() { return this.identy; }

    @Override
    public SupportedTypes getComponentType() { return this.componenttype; }
    //endregion

    public GreenGenerationSpecification getData() {
        return data;
    }

    public void setData(GreenGenerationSpecification data) {
        this.data = data;
    }

    public static GreenGeneration create(String identy, SupportedGenerations generation) {
        if (identy == null || identy.trim().isEmpty()) {
            throw new IllegalArgumentException(Messages.ER_0);
        }
        GreenGeneration res = new GreenGeneration();
        res.identy = identy;
        res.setData(GreenGenerationSpecification.createDefault(generation));
        return res;
    }
}
