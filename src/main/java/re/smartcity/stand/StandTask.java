package re.smartcity.stand;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

@Configuration
public class StandTask {

    private final Logger logger = LoggerFactory.getLogger(StandTask.class);

    @Autowired
    private StandService service;

    @PostConstruct
    public void appPostStart() {
        logger.info("--> запуск сервиса управления стендом.");
        service.start();
    }

    @PreDestroy
    public void appShutdown() {
        logger.info("--> завершение сервиса управления стендом.");
        service.stop();
    }
}
