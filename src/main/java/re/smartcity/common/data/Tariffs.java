package re.smartcity.common.data;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import re.smartcity.common.data.exchange.TariffsData;

@Data
@NoArgsConstructor
@Table("configurations")
public class Tariffs {

    public static final String key = "tariffs.cfg";

    @Id
    private final String id = key;

    private TariffsData data = new TariffsData();
}
