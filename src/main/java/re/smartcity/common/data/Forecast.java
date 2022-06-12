package re.smartcity.common.data;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Data
@NoArgsConstructor
@Table("forecast")
public class Forecast {

    @Id
    private Long id;

    @NonNull
    private String name;
    @NonNull
    private ForecastTypes fc_type;
    @NonNull
    private ForecastPoint[] data;
}
