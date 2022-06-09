package re.smartcity.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import re.smartcity.data.TestBean;
import re.smartcity.handlers.SmartCityResourceHandler;
import re.smartcity.task.TestService;

import javax.annotation.PreDestroy;

@Configuration
public class SmartCity {

    private final Logger logger = LoggerFactory.getLogger(SmartCityResourceHandler.class);

    @Bean
    public TestBean testBean() {
        logger.info("--> тестовый бин создан.");
        return new TestBean();
    }

    @Bean
    public TestService testService() {
        logger.info("--> тестовый сервис создан.");
        return new TestService();
    }
}
