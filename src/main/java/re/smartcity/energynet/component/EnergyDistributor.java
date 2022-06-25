package re.smartcity.energynet.component;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import re.smartcity.common.resources.Messages;
import re.smartcity.energynet.*;
import re.smartcity.energynet.component.data.EnergyDistributorSpecification;

@Table("component")
public class EnergyDistributor implements IComponentIdentification {

    @Id
    private String identy; // уникальный идентификатор
    private final SupportedTypes componenttype = SupportedTypes.DISTRIBUTOR;

    private volatile EnergyDistributorSpecification data;

    public EnergyDistributor() { }

    //region IComponentIdentification
    @Override
    public String getIdenty() { return this.identy; }

    @Override
    public SupportedTypes getComponentType() { return this.componenttype; }
    //endregion

    public EnergyDistributorSpecification getData() {
        return data;
    }

    public void setData(EnergyDistributorSpecification data) {
        this.data = data;
    }

    public static EnergyDistributor create(String identy) {
        if (identy == null || identy.trim().isEmpty()) {
            throw new IllegalArgumentException(Messages.ER_0);
        }
        EnergyDistributor res = new EnergyDistributor();
        res.identy = identy;
        res.setData(EnergyDistributorSpecification.createDefault(identy));
        return res;
    }
}
