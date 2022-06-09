package re.smartcity.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import re.smartcity.data.TestBean;
import re.smartcity.handlers.SmartCityResourceHandler;
import re.smartcity.task.TestService;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

@Configuration
public class TestTask {

    private final Logger logger = LoggerFactory.getLogger(SmartCityResourceHandler.class);

    @Autowired
    private TestService service;
    @Autowired
    private TestBean bean;

    @PostConstruct
    public void appPreStart() {
        logger.info("--> сборка приложения завершена.");
        service.start(bean);
    }

    @PreDestroy
    public void appShutdown() {
        logger.info("--> завершение приложения.");
        service.stop();
    }
}
