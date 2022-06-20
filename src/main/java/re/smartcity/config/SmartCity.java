package re.smartcity.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.r2dbc.convert.R2dbcCustomConversions;
import org.springframework.data.r2dbc.dialect.MySqlDialect;
import re.smartcity.common.converters.*;
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
        converters.add(new JsonDataAType_ReadConverter());
        converters.add(new JsonDataAType_WriteConverter());
        converters.add(new JsonDataBType_ReadConverter());
        converters.add(new JsonDataBType_WriteConverter());
        return R2dbcCustomConversions.of(MySqlDialect.INSTANCE, converters);
    }

    // инфраструктура управление ветром
    @Bean
    public WindStatusData windStatusData() {
        return new WindStatusData();
    }

    @Bean
    public WindControlData windControlData() {
        return new WindControlData();
    }

    // инфраструктура управление солнцем
    @Bean
    public SunStatusData sunStatusData() {
        return new SunStatusData();
    }

    @Bean
    public SunControlData sunControlData() {
        return new SunControlData();
    }

}
