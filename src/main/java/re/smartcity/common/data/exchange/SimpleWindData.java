package re.smartcity.common.data.exchange;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public final class SimpleWindData {

    private int power = 0;
    private String url = "";
}
