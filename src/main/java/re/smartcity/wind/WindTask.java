package re.smartcity.wind;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

@Configuration
public class WindTask {

    @Autowired
    private WindRouterHandlers routerHandlers;

    @PostConstruct
    public void appPostStart() {
        routerHandlers.internalSetOff();
    }

    @PreDestroy
    public void appShutdown() {
        routerHandlers.internalSetOff();
        try {
            Thread.sleep(300); // !!! делаю просто "небольшую" задержку, предполагая, что команда "ушла".
        }
        catch (InterruptedException ignored) { }
    }
}
