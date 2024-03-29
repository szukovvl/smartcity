package re.smartcity.energynet.component.data;

import com.fasterxml.jackson.annotation.JsonInclude;
import re.smartcity.common.data.Forecast;
import re.smartcity.energynet.*;
import re.smartcity.energynet.component.ElectricalSubnet;

public class ConsumerSpecification implements ISpecifications {

    public ConsumerSpecification() { }

    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    private volatile Forecast forecast; // прогноз

    private volatile boolean useforecast = false; // задействовать прогноз

    private volatile double energy = 0.8; // максимальная мощность в МВт

    private volatile double carbon = 0; // выброс CO2 (экология)

    private volatile SupportedPriceCategories catprice = SupportedPriceCategories.CATEGORY_1; // ценовая категория

    private volatile SupportedVoltageLevels voltagelevel = SupportedVoltageLevels.AVG_VOLTAGE_1; // уровень напряжения

    private volatile ElectricalSubnet[] inputs; // энерговвод

    private volatile SupportedConsumers consumertype; // категория надежности электроснабжения

    //region характеристики
    public Forecast getForecast() {
        return forecast;
    }

    public void setForecast(Forecast forecast) {
        this.forecast = forecast;
    }

    public boolean isUseforecast() {
        return useforecast;
    }

    public void setUseforecast(boolean useforecast) {
        this.useforecast = useforecast;
    }

    public double getEnergy() {
        return energy;
    }

    public void setEnergy(double energy) {
        this.energy = energy;
    }

    public double getCarbon() {
        return carbon;
    }

    public void setCarbon(double carbon) {
        this.carbon = carbon;
    }

    public SupportedPriceCategories getCatprice() {
        return catprice;
    }

    public void setCatprice(SupportedPriceCategories catprice) {
        this.catprice = catprice;
    }

    public SupportedVoltageLevels getVoltagelevel() {
        return voltagelevel;
    }

    public void setVoltagelevel(SupportedVoltageLevels voltagelevel) {
        this.voltagelevel = voltagelevel;
    }

    public ElectricalSubnet[] getInputs() {
        return inputs;
    }

    public void setInputs(ElectricalSubnet[] inputs) {
        this.inputs = inputs;
    }

    public SupportedConsumers getConsumertype() {
        return consumertype;
    }

    public void setConsumertype(SupportedConsumers consumertype) {
        this.consumertype = consumertype;
    }
    //endregion

    public static ConsumerSpecification createDefault(SupportedConsumers consumer, byte[] inputs) {
        ConsumerSpecification res = new ConsumerSpecification();
        res.setConsumertype(consumer);
        ElectricalSubnet[] lines = new ElectricalSubnet[inputs.length];
        for (int i = 0; i < lines.length; i++) {
            lines[i] = ElectricalSubnet.create("#" + (i + 1), inputs[i]);
        }
        res.setInputs(lines);

        return res;
    }
}
