package re.smartcity.energynet.component;

import com.fasterxml.jackson.annotation.*;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import re.smartcity.common.resources.Messages;
import re.smartcity.energynet.IComponentIdentification;
import re.smartcity.energynet.SupportedTypes;
import re.smartcity.energynet.component.data.MainSubstationSpecification;

@Table("component")
public class MainSubstationPowerSystem implements IComponentIdentification {

    // одинаковые для всех
    @Id
    private String identy; // уникальный идентификатор

    private final SupportedTypes componenttype = SupportedTypes.MAINSUBSTATION; // тип компонента

    private volatile MainSubstationSpecification data;

    public MainSubstationPowerSystem() { }

    //region IComponentIdentification
    @Override
    public String getIdenty() { return this.identy; }

    @Override
    public SupportedTypes getComponentType() { return this.componenttype; }
    //endregion

    public MainSubstationSpecification getData() {
        return data;
    }

    public void setData(MainSubstationSpecification data) {
        this.data = data;
    }

    public static MainSubstationPowerSystem create(String identy) {
        if (identy == null || identy.trim().isEmpty()) {
            throw new IllegalArgumentException(Messages.ER_0);
        }
        MainSubstationPowerSystem res = new MainSubstationPowerSystem();
        res.identy = identy;
        res.setData(MainSubstationSpecification.createDefault(identy));
        return res;
    }
}
