package re.smartcity.common.data.exchange;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class TariffsData {

    private double trade_price = 0.0;
    private double tech_price = 0.0;

    private double t_service = 0.0;
    private double t_total = 0.0;

    private TransmissionServices tariff = new TransmissionServices();
    private SellersMarkup sales_allowance = new SellersMarkup();
    private TariffThreeZones t_zone_3 = new TariffThreeZones();
    private TariffTwoZones t_zone_2 = new TariffTwoZones();
    private AdditionalGenerationTariff t_alternative = new AdditionalGenerationTariff();

}
