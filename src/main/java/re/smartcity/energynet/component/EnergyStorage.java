package re.smartcity.energynet.component;

import org.springframework.data.relational.core.mapping.Table;
import re.smartcity.common.resources.Messages;
import re.smartcity.energynet.*;
import re.smartcity.energynet.component.data.EnergyStorageSpecification;

import java.time.LocalTime;

@Table("component")
public class EnergyStorage implements IComponentIdentification, IEnergyStorage, IGeneration {

    private String identy; // уникальный идентификатор
    private final SupportedTypes componenttype = SupportedTypes.STORAGE; // тип компонента

    private volatile EnergyStorageSpecification data;

    public EnergyStorage() { }

    //region IComponentIdentification
    @Override
    public String getIdenty() { return this.identy; }

    @Override
    public SupportedTypes getComponentType() { return this.componenttype; }
    //endregion

    public EnergyStorageSpecification getData() {
        return data;
    }

    public void setData(EnergyStorageSpecification data) {
        this.data = data;
    }

    public static EnergyStorage create(String identy) {
        if (identy == null || identy.trim().isEmpty()) {
            throw new IllegalArgumentException(Messages.ER_0);
        }
        EnergyStorage res = new EnergyStorage();
        res.identy = identy;
        res.setData(EnergyStorageSpecification.createDefault());
        return res;
    }
}
