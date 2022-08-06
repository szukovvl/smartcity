package re.smartcity.sun;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import re.smartcity.common.CommonStorage;
import re.smartcity.common.data.exchange.SunConfiguration;
import reactor.core.publisher.Mono;

import javax.annotation.PostConstruct;

@Configuration
public class SunTask {

    @Autowired
    private SunStatusData sunStatus;

    @Autowired
    private CommonStorage storage;

    @PostConstruct
    public void appPostStart() {
        storage.getAndCreate(SunConfiguration.key, SunConfiguration.class)
                .map(data -> {
                    sunStatus.apply(data.getData());
                    return data;
                })
                .subscribe();
    }
}
