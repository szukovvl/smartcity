package re.smartcity.energynet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import re.smartcity.energynet.component.MainSubstationPowerSystem;
import reactor.core.publisher.Flux;

import javax.annotation.PostConstruct;

@Configuration
public class EnergynetConfiguration {

    private final Logger logger = LoggerFactory.getLogger(EnergynetConfiguration.class);

    @Autowired
    private EnergynetStorage storage;

    @PostConstruct
    public void appPreStart() {
        logger.info("--> конфигурация энергосети.");

        // 1. выбираю главные подстанции
        Flux<IComponentIdentification> items = storage.find(SupportedTypes.MAINSUBSTATION)
                .map(e -> {
                    System.out.println(e);
                    return e;
                });
        items.switchIfEmpty(e -> {
            logger.info("--> конфигурация энергосети не задана.");
            storage.insert(MainSubstationPowerSystem.create("Гп-1"))
                    .map(a -> {
                        System.out.println(a);
                        return e;
                    })
                    .subscribe();
            storage.insert(MainSubstationPowerSystem.create("Гп-2"))
                    .map(a -> {
                        System.out.println(a);
                        return e;
                    })
                    .subscribe();
        }).subscribe();
    }
}
