package re.smartcity.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.r2dbc.convert.R2dbcCustomConversions;
import org.springframework.data.r2dbc.dialect.MySqlDialect;
import org.springframework.web.reactive.function.client.WebClient;
import re.smartcity.common.converters.*;
import re.smartcity.modeling.ModelingData;
import re.smartcity.stand.StandStatusData;
import re.smartcity.sun.SunStatusData;
import re.smartcity.wind.WindStatusData;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class SmartCity {

    // private final Logger logger = LoggerFactory.getLogger(SmartCity.class);

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
        converters.add(new Tariffs_Data_ReadConverter());
        converters.add(new Tariffs_Data_WriteConverter());
        converters.add(new GameCriteria_Data_ReadConverter());
        converters.add(new GameCriteria_Data_WriteConverter());
        converters.add(new SimpleWind_Data_ReadConverter());
        converters.add(new SimpleWind_Data_WriteConverter());
        converters.add(new SimpleStand_Data_ReadConverter());
        converters.add(new SimpleStand_Data_WriteConverter());
        converters.add(new SimpleSun_Data_ReadConverter());
        converters.add(new SimpleSun_Data_WriteConverter());
        return R2dbcCustomConversions.of(MySqlDialect.INSTANCE, converters);
    }

    //region инфраструктура управление ветром
    @Bean
    public WindStatusData windStatusData() {
        return new WindStatusData();
    }

    @Bean
    public WebClient webClient() {
        return WebClient
                .builder()
                .build();
    }
    //endregion

    //region инфраструктура управление солнцем
    @Bean
    public SunStatusData sunStatusData() {
        return new SunStatusData();
    }
    //endregion

    //region инфраструктура стэнда
    @Bean
    public StandStatusData standStatusData() { return new StandStatusData(); }
    //endregion

    //region инфраструктура модели
    @Bean
    public ModelingData modelingData() { return new ModelingData(); }
    //endregion
}
