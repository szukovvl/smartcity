package re.smartcity.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import re.smartcity.wind.WindControlData;
import re.smartcity.wind.WindService;
import re.smartcity.wind.WindStatusData;

@Configuration
public class SmartCity {

    // инфраструктура управление ветром
    @Bean
    public WindStatusData windStatusData() {
        return new WindStatusData();
    }

    @Bean
    public WindControlData windControlData() {
        return new WindControlData();
    }

    @Bean
    public WindService windService() {
        return new WindService();
    }
}
