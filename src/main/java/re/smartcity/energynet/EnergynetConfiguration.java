package re.smartcity.energynet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import re.smartcity.energynet.component.*;
import re.smartcity.modeling.ModelingData;
import reactor.core.publisher.Flux;

import javax.annotation.PostConstruct;
import java.util.concurrent.Executors;

@Configuration
public class EnergynetConfiguration {

    private final Logger logger = LoggerFactory.getLogger(EnergynetConfiguration.class);

    @Autowired
    private EnergynetStorage storage;

    @Autowired
    private ModelingData model;

    private void prepareConfiguration() {
        logger.info("--> конфигурация энергосети.");

        // 1. выбираю главные подстанции
        MainSubstationPowerSystem[] mainSubstations = storage.find(SupportedTypes.MAINSUBSTATION, MainSubstationPowerSystem.class)
                .toStream()
                .toArray(MainSubstationPowerSystem[]::new);

        IComponentIdentification[] elements;

        if (mainSubstations.length == 0)
        {
            logger.info("--> элементы энергосети не заданы.");
            logger.info("--> генерация элементов энергосети.");

            mainSubstations = Flux.merge(
                            storage.insert(MainSubstationPowerSystem.create("Гп-1")),
                            storage.insert(MainSubstationPowerSystem.create("Гп-2"))
                    )
                    .toStream()
                    .toArray(MainSubstationPowerSystem[]::new);

            elements = Flux.merge(
                            Flux.merge(
                                    storage.insert(EnergyDistributor.create("Мп-1")),
                                    storage.insert(EnergyDistributor.create("Мп-2"))
                            ),
                            Flux.merge(
                                    storage.insert(Generation.create("Дг-1")),
                                    storage.insert(Generation.create("Дг-2")),
                                    storage.insert(Generation.create("Дг-3")),
                                    storage.insert(Generation.create("Дг-4")),
                                    storage.insert(Generation.create("Дг-5"))
                            ),
                            Flux.merge(
                                    storage.insert(GreenGeneration.create("Сг-1", SupportedGenerations.SOLAR)),
                                    storage.insert(GreenGeneration.create("Сг-2", SupportedGenerations.SOLAR)),
                                    storage.insert(GreenGeneration.create("Сг-3", SupportedGenerations.SOLAR)),
                                    storage.insert(GreenGeneration.create("Сг-4", SupportedGenerations.SOLAR)),
                                    storage.insert(GreenGeneration.create("Сг-5", SupportedGenerations.SOLAR))
                            ),
                            Flux.merge(
                                    storage.insert(GreenGeneration.create("Вг-1", SupportedGenerations.WIND)),
                                    storage.insert(GreenGeneration.create("Вг-2", SupportedGenerations.WIND)),
                                    storage.insert(GreenGeneration.create("Вг-3", SupportedGenerations.WIND)),
                                    storage.insert(GreenGeneration.create("Вг-4", SupportedGenerations.WIND)),
                                    storage.insert(GreenGeneration.create("Вг-5", SupportedGenerations.WIND))
                            ),
                            Flux.merge(
                                    storage.insert(EnergyStorage.create("ЭХакб-1")),
                                    storage.insert(EnergyStorage.create("ЭХакб-2")),
                                    storage.insert(EnergyStorage.create("ЭХакб-3")),
                                    storage.insert(EnergyStorage.create("ЭХакб-4")),
                                    storage.insert(EnergyStorage.create("ЭХакб-5"))
                            ),
                            Flux.merge(
                                    storage.insert(Consumer.create("ЖМ-1", SupportedConsumers.DISTRICT)),
                                    storage.insert(Consumer.create("ЖМ-2", SupportedConsumers.DISTRICT)),
                                    storage.insert(Consumer.create("ЖМ-3", SupportedConsumers.DISTRICT)),
                                    storage.insert(Consumer.create("ЖМ-4", SupportedConsumers.DISTRICT)),
                                    storage.insert(Consumer.create("ЖМ-5", SupportedConsumers.DISTRICT))
                            ),
                            Flux.merge(
                                    storage.insert(Consumer.create("П-1", SupportedConsumers.INDUSTRY)),
                                    storage.insert(Consumer.create("П-2", SupportedConsumers.INDUSTRY)),
                                    storage.insert(Consumer.create("П-3", SupportedConsumers.INDUSTRY)),
                                    storage.insert(Consumer.create("П-4", SupportedConsumers.INDUSTRY)),
                                    storage.insert(Consumer.create("П-5", SupportedConsumers.INDUSTRY))
                            ),
                            Flux.merge(
                                    storage.insert(Consumer.create("С-1", SupportedConsumers.HOSPITAL)),
                                    storage.insert(Consumer.create("С-2", SupportedConsumers.HOSPITAL)),
                                    storage.insert(Consumer.create("С-3", SupportedConsumers.HOSPITAL)),
                                    storage.insert(Consumer.create("С-4", SupportedConsumers.HOSPITAL)),
                                    storage.insert(Consumer.create("С-5", SupportedConsumers.HOSPITAL))
                            )
                    )
                    .toStream()
                    .toArray(IComponentIdentification[]::new);

            logger.info("--> генерация элементов энергосети завершена: добавлено {} элементов и {} главных подстанции.",
                    elements.length, mainSubstations.length);
        } else {
            elements = Flux.merge(
                            storage.find(SupportedTypes.DISTRIBUTOR, EnergyDistributor.class),
                            storage.find(SupportedTypes.GREEGENERATOR, GreenGeneration.class),
                            storage.find(SupportedTypes.GENERATOR, Generation.class),
                            storage.find(SupportedTypes.STORAGE, EnergyStorage.class),
                            storage.find(SupportedTypes.CONSUMER, Consumer.class)
                    )
                    .toStream()
                    .toArray(IComponentIdentification[]::new);

            logger.info("--> {} элементов энергосети и {} главных подстанции.",
                    elements.length, mainSubstations.length);
        }

        model.setAllobjects(elements);
        model.putOnMonitoring(mainSubstations);
    }

    @PostConstruct
    public void appPreStart() {

        Executors.newSingleThreadExecutor().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    prepareConfiguration();
                }
                catch (Exception ex) {
                    logger.error("Ошибка подготовки элементов энергосети", ex);
                }
            }
        });

    }
}
