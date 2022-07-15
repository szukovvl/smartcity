package re.smartcity.energynet.component.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import re.smartcity.common.data.Forecast;
import re.smartcity.energynet.*;
import re.smartcity.energynet.component.ElectricalSubnet;

public class ConsumerSpecification implements IComponentManagement {

    @JsonIgnore
    private volatile boolean isactive;

    @JsonIgnore
    private volatile ConsumerInstantValues instantValues;

    @JsonIgnore
    private volatile ConsumerStackedValues stackedValues;

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

    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    private volatile IGeneration generation; // собственная генерация

    //region IComponentManagement
    @Override
    public boolean getIsactive() { return this.isactive; }

    @Override
    public void setIsactive(boolean isactive) { this.isactive = isactive; }
    //endregion

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

    public IGeneration getGeneration() {
        return generation;
    }

    public void setGeneration(IGeneration generation) {
        this.generation = generation;
    }
    //endregion

    //region оперативные данные модели
    //region мгновенные значения
    public ConsumerInstantValues getInstantValues() {
        return instantValues;
    }

    public void setInstantValues(ConsumerInstantValues instantValues) {
        this.instantValues = instantValues;
    }
    //endregion

    //region значения с накоплением
    public ConsumerStackedValues getStackedValues() {
        return stackedValues;
    }

    public void setStackedValues(ConsumerStackedValues stackedValues) {
        this.stackedValues = stackedValues;
    }
    //endregion
    //endregion

    public static ConsumerSpecification createDefault(SupportedConsumers consumer) {
        ConsumerSpecification res = new ConsumerSpecification();
        res.setConsumertype(consumer);
        switch (consumer) {
            case HOSPITAL:
            case INDUSTRY:
                res.setInputs(new ElectricalSubnet[2]);
                break;
            case DISTRICT:
                res.setInputs(new ElectricalSubnet[1]);
                break;
        }
        return res;
    }
}
