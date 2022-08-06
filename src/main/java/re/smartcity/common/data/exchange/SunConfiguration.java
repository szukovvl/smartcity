package re.smartcity.common.data.exchange;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Data
@NoArgsConstructor
@Table("configurations")
public class SunConfiguration {

    public static final String key = "sun.cfg";

    @Id
    private final String id = key;

    private SimpleSunData data = new SimpleSunData();
}
