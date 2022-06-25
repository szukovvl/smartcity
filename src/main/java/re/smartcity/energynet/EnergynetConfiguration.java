package re.smartcity.energynet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import re.smartcity.energynet.component.EnergyDistributor;
import re.smartcity.energynet.component.Generation;
import re.smartcity.energynet.component.GreenGeneration;
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
        // 2. выбираю остальные объекты энергосистемы
        // 3. формирую обобщенный блок данных
        // 4. формирую потоки по количеству главных постанций для обработки динамических данных энергосистемы
        // 5. где-то ранее необходимо запустить блок управления стендом (компоненты энергосистемы)
        // 6. все компоненты, не являющиеся главными посьанциями, заносятся в список доступных объектов
        Flux<IComponentIdentification> items = storage.find(SupportedTypes.MAINSUBSTATION, MainSubstationPowerSystem.class)
                .map(e -> {
                    System.out.println(String.format("--> %s: %s", e.getIdenty(), e.getComponentType()));
                    return e;
                });
        storage.find(SupportedTypes.DISTRIBUTOR, EnergyDistributor.class)
                        .map(e -> {
                            System.out.println(String.format("--> %s: %s", e.getIdenty(), e.getComponentType()));
                            return e;
                        })
                        .subscribe();
        storage.find(SupportedTypes.GREEGENERATOR, GreenGeneration.class)
                .map(e -> {
                    System.out.println(String.format("--> %s: %s", e.getIdenty(), e.getComponentType()));
                    return e;
                })
                .subscribe();
        storage.find(SupportedTypes.GENERATOR, Generation.class)
                .map(e -> {
                    System.out.println(String.format("--> %s: %s", e.getIdenty(), e.getComponentType()));
                    return e;
                })
                .subscribe();

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

            storage.insert(EnergyDistributor.create("Мп-1"))
                    .map(a -> {
                        System.out.println(a);
                        return e;
                    })
                    .subscribe();
            storage.insert(EnergyDistributor.create("Мп-2"))
                    .map(a -> {
                        System.out.println(a);
                        return e;
                    })
                    .subscribe();

            storage.insert(Generation.create("Дг-1"))
                    .map(a -> {
                        System.out.println(a);
                        return e;
                    })
                    .subscribe();
            storage.insert(Generation.create("Дг-2"))
                    .map(a -> {
                        System.out.println(a);
                        return e;
                    })
                    .subscribe();
            storage.insert(Generation.create("Дг-3"))
                    .map(a -> {
                        System.out.println(a);
                        return e;
                    })
                    .subscribe();
            storage.insert(Generation.create("Дг-4"))
                    .map(a -> {
                        System.out.println(a);
                        return e;
                    })
                    .subscribe();
            storage.insert(Generation.create("Дг-5"))
                    .map(a -> {
                        System.out.println(a);
                        return e;
                    })
                    .subscribe();

            storage.insert(GreenGeneration.create("Сг-1", SupportedGenerations.SOLAR))
                    .map(a -> {
                        System.out.println(a);
                        return e;
                    })
                    .subscribe();
            storage.insert(GreenGeneration.create("Сг-2", SupportedGenerations.SOLAR))
                    .map(a -> {
                        System.out.println(a);
                        return e;
                    })
                    .subscribe();
            storage.insert(GreenGeneration.create("Сг-3", SupportedGenerations.SOLAR))
                    .map(a -> {
                        System.out.println(a);
                        return e;
                    })
                    .subscribe();
            storage.insert(GreenGeneration.create("Сг-4", SupportedGenerations.SOLAR))
                    .map(a -> {
                        System.out.println(a);
                        return e;
                    })
                    .subscribe();
            storage.insert(GreenGeneration.create("Сг-5", SupportedGenerations.SOLAR))
                    .map(a -> {
                        System.out.println(a);
                        return e;
                    })
                    .subscribe();

            storage.insert(GreenGeneration.create("Вг-1", SupportedGenerations.WIND))
                    .map(a -> {
                        System.out.println(a);
                        return e;
                    })
                    .subscribe();
            storage.insert(GreenGeneration.create("Вг-2", SupportedGenerations.WIND))
                    .map(a -> {
                        System.out.println(a);
                        return e;
                    })
                    .subscribe();
            storage.insert(GreenGeneration.create("Вг-3", SupportedGenerations.WIND))
                    .map(a -> {
                        System.out.println(a);
                        return e;
                    })
                    .subscribe();
            storage.insert(GreenGeneration.create("Вг-4", SupportedGenerations.WIND))
                    .map(a -> {
                        System.out.println(a);
                        return e;
                    })
                    .subscribe();
            storage.insert(GreenGeneration.create("Вг-5", SupportedGenerations.WIND))
                    .map(a -> {
                        System.out.println(a);
                        return e;
                    })
                    .subscribe();
        }).subscribe();
    }
}
