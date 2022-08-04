package re.smartcity.wind;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import re.smartcity.common.CommonStorage;
import re.smartcity.common.data.exchange.WindConfiguration;
import reactor.core.publisher.Mono;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

@Configuration
public class WindTask {

    @Autowired
    private WindStatusData windStatus;

    @Autowired
    private CommonStorage storage;

    @Autowired
    private WindRouterHandlers routerHandlers;

    @PostConstruct
    public void appPostStart() {
        storage.getAndCreate(WindConfiguration.key, WindConfiguration.class)
                .map(data -> {
                    windStatus.apply(data.getData());
                    routerHandlers.internalSetOff();
                    return Mono.empty();
                })
                .subscribe();
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
