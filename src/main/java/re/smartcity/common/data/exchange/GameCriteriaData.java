package re.smartcity.common.data.exchange;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class GameCriteriaData {

    private double power_balance = 0.0;
    private double economic = 0.0;
    private double ecology = 0.0;

    private FactorsPowerBalance coef_power_balance = new FactorsPowerBalance();
    private FactorsEconomic coef_economic = new FactorsEconomic();
    private FactorsEcology coef_ecology = new FactorsEcology();

}
