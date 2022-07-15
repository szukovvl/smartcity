package re.smartcity.energynet.component.data.client;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;
import re.smartcity.common.data.Forecast;
import re.smartcity.common.resources.Messages;
import re.smartcity.energynet.SupportedPriceCategories;
import re.smartcity.energynet.SupportedVoltageLevels;
import re.smartcity.energynet.component.data.ConsumerSpecification;

@Data
@NoArgsConstructor
public final class SmallConsumerSpecification {

    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    private Forecast forecast; // прогноз

    private boolean useforecast; // задействовать прогноз

    private double energy; // максимальная мощность в МВт

    private int carbon = 0; // выброс CO2 (экология)

    private SupportedPriceCategories catprice = SupportedPriceCategories.CATEGORY_1; // ценовая категория

    private SupportedVoltageLevels voltagelevel = SupportedVoltageLevels.AVG_VOLTAGE_1; // уровень напряжения

    public static void validate(SmallConsumerSpecification data) {
        if (data.getEnergy() < 0.0) {
            throw new IllegalArgumentException(Messages.ER_1);
        }
        if (data.isUseforecast() && data.getForecast() == null) {
            throw new IllegalArgumentException(Messages.ER_2);
        }
        if (data.getCarbon() < 0) {
            throw new IllegalArgumentException(Messages.ER_4);
        }
    }

    public static void AssignTo(SmallConsumerSpecification src, ConsumerSpecification dest) {
        dest.setEnergy(src.getEnergy());
        dest.setUseforecast(src.isUseforecast());
        dest.setForecast(src.getForecast());
        dest.setCatprice(src.getCatprice());
        dest.setVoltagelevel(src.getVoltagelevel());
        dest.setCarbon(src.getCarbon());
    }
}
