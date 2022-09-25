package re.smartcity.config.sockets.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public final class ResponseSchemeData {

    private int gamerkey;
    private int substation;

    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    private int[] consumers;

    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    private int[] tcconsumers;

    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    private double tcprice;

    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    private PurchasedLot[] generators;

    public static ResponseSchemeDataBuilder build(int key) {
        return new ResponseSchemeDataBuilder(key);
    }
}
