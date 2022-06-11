package re.smartcity.wind;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

@Configuration
public class WindControlTask {

    private final Logger logger = LoggerFactory.getLogger(WindControlTask.class);

    @Autowired
    private WindService windService;

    @PostConstruct
    public void appPreStart() {
        logger.info("--> запуск сервиса управления ветром.");
        windService.start();
    }

    @PreDestroy
    public void appShutdown() {
        logger.info("--> завершение сервиса управления ветром.");
        windService.stop();
    }
}
