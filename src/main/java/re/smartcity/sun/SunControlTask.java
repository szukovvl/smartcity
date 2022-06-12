package re.smartcity.sun;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

@Configuration
public class SunControlTask {

    private final Logger logger = LoggerFactory.getLogger(SunControlTask.class);

    @Autowired
    private SunService sunService;

    @PostConstruct
    public void appPreStart() {
        logger.info("--> запуск сервиса управления солнцем.");
        sunService.start();
    }

    @PreDestroy
    public void appShutdown() {
        logger.info("--> завершение сервиса управления солнцем.");
        sunService.stop();
    }
}
