package re.smartcity.modeling.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import re.smartcity.energynet.component.Consumer;
import re.smartcity.energynet.component.EnergyDistributor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GamerData {
    private Consumer[] defConsumers;
    private EnergyDistributor substation;
}
