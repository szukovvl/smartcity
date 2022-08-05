package re.smartcity.common.data.exchange;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import re.smartcity.stand.StandControlData;

@Data
@NoArgsConstructor
@Table("configurations")
public final class StandConfiguration {

    public static final String key = "stand.cfg";

    @Id
    private final String id = key;

    private StandControlData data = new StandControlData();
}
