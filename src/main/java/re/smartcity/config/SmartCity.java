package re.smartcity.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.r2dbc.convert.R2dbcCustomConversions;
import org.springframework.data.r2dbc.dialect.MySqlDialect;
import re.smartcity.common.converters.*;
import re.smartcity.modeling.ModelingData;
import re.smartcity.stand.StandControlData;
import re.smartcity.sun.SunControlData;
import re.smartcity.sun.SunStatusData;
import re.smartcity.wind.WindControlData;
import re.smartcity.wind.WindStatusData;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class SmartCity {

    @Bean
    public R2dbcCustomConversions ForecastPointConversions() {
        List<Converter<?, ?>> converters = new ArrayList<>();
        converters.add(new JsonFieldReadConverter());
        converters.add(new JsonFieldWriteConverter());
        converters.add(new MainSubstation_Data_ReadConverter());
        converters.add(new MainSubstation_Data_WriteConverter());
        converters.add(new EnergyDistributor_Data_ReadConverter());
        converters.add(new EnergyDistributor_Data_WriteConverter());
        converters.add(new Generation_Data_ReadConverter());
        converters.add(new Generation_Data_WriteConverter());
        converters.add(new GreenGeneration_Data_ReadConverter());
        converters.add(new GreenGeneration_Data_WriteConverter());
        converters.add(new EnergyStorage_Data_ReadConverter());
        converters.add(new EnergyStorage_Data_WriteConverter());
        converters.add(new Consumer_Data_ReadConverter());
        converters.add(new Consumer_Data_WriteConverter());
        return R2dbcCustomConversions.of(MySqlDialect.INSTANCE, converters);
    }

    //region инфраструктура управление ветром
    @Bean
    public WindStatusData windStatusData() {
        return new WindStatusData();
    }

    @Bean
    public WindControlData windControlData() {
        return new WindControlData();
    }
    //endregion

    //region инфраструктура управление солнцем
    @Bean
    public SunStatusData sunStatusData() {
        return new SunStatusData();
    }

    @Bean
    public SunControlData sunControlData() {
        return new SunControlData();
    }
    //endregion

    //region инфраструктура управления стендом
    @Bean
    public StandControlData standControlData() { return new StandControlData(); }
    //endregion

    //region инфраструктура модели
    @Bean
    public ModelingData modelingData() { return new ModelingData(); }
    //endregion
}
