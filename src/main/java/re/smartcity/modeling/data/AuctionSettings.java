package re.smartcity.modeling.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public final class AuctionSettings {
    private double startingcost = 1440.0; // стоимость в сутки
    private int lotwaiting = 30; // время в сек.
    private int auctionstep = 10; // шаг аукциона в процентах
}
