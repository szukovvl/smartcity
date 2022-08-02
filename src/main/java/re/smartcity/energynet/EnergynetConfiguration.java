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
        MainSubstationPowerSystem[] mainSubstations = storage
                .find(SupportedTypes.MAINSUBSTATION, MainSubstationPowerSystem.class)
                .toStream()
                .toArray(MainSubstationPowerSystem[]::new);

        IComponentIdentification[] elements;

        if (mainSubstations.length == 0)
        {
            logger.info("--> элементы энергосети не заданы.");
            logger.info("--> генерация элементов энергосети.");

            mainSubstations = Flux.merge(
                            storage.insert(MainSubstationPowerSystem.create("Гп-1", (byte) 0x62)),
                            storage.insert(MainSubstationPowerSystem.create("Гп-2", (byte) 0x63))
                    )
                    .toStream()
                    .toArray(MainSubstationPowerSystem[]::new);

            elements = Flux.merge(
                            Flux.merge(
                                    storage.insert(EnergyDistributor.create("Мп-1", (byte) 0x64)),
                                    storage.insert(EnergyDistributor.create("Мп-2", (byte) 0x65))
                            ),
                            Flux.merge(
                                    storage.insert(Generation.create("Дг-1", (byte) 0x03)),
                                    storage.insert(Generation.create("Дг-2", (byte) 0x04)),
                                    storage.insert(Generation.create("Дг-3", (byte) 0x05)),
                                    storage.insert(Generation.create("Дг-4", (byte) 0x06)),
                                    storage.insert(Generation.create("Дг-5", (byte) 0x07))
                            ),
                            Flux.merge(
                                    storage.insert(GreenGeneration.create("Сг-1", (byte) 0x16, SupportedGenerations.SOLAR)),
                                    storage.insert(GreenGeneration.create("Сг-2", (byte) 0x17, SupportedGenerations.SOLAR)),
                                    storage.insert(GreenGeneration.create("Сг-3", (byte) 0x18, SupportedGenerations.SOLAR)),
                                    storage.insert(GreenGeneration.create("Сг-4", (byte) 0x19, SupportedGenerations.SOLAR)),
                                    storage.insert(GreenGeneration.create("Сг-5", (byte) 0x1A, SupportedGenerations.SOLAR))
                            ),
                            Flux.merge(
                                    storage.insert(GreenGeneration.create("Вг-1", (byte) 0x1B, SupportedGenerations.WIND)),
                                    storage.insert(GreenGeneration.create("Вг-2", (byte) 0x1C, SupportedGenerations.WIND)),
                                    storage.insert(GreenGeneration.create("Вг-3", (byte) 0x1D, SupportedGenerations.WIND))
                            ),
                            Flux.merge(
                                    storage.insert(EnergyStorage.create("ЭХакб-1", (byte) 0x08)),
                                    storage.insert(EnergyStorage.create("ЭХакб-2", (byte) 0x09)),
                                    storage.insert(EnergyStorage.create("ЭХакб-3", (byte) 0x0A)),
                                    storage.insert(EnergyStorage.create("ЭХакб-4", (byte) 0x0B)),
                                    storage.insert(EnergyStorage.create("ЭХакб-5", (byte) 0x0C))
                            ),
                            Flux.merge(
                                    storage.insert(Consumer.create("ЖМ-1", (byte) 0x0D, SupportedConsumers.DISTRICT)),
                                    storage.insert(Consumer.create("ЖМ-2", (byte) 0x0E, SupportedConsumers.DISTRICT)),
                                    storage.insert(Consumer.create("ЖМ-3", (byte) 0x0F, SupportedConsumers.DISTRICT)),
                                    storage.insert(Consumer.create("ЖМ-4", (byte) 0x10, SupportedConsumers.DISTRICT)),
                                    storage.insert(Consumer.create("ЖМ-5", (byte) 0x11, SupportedConsumers.DISTRICT)),
                                    storage.insert(Consumer.create("ЖМ-6", (byte) 0x12, SupportedConsumers.DISTRICT)),
                                    storage.insert(Consumer.create("ЖМ-7", (byte) 0x13, SupportedConsumers.DISTRICT)),
                                    storage.insert(Consumer.create("ЖМ-8", (byte) 0x14, SupportedConsumers.DISTRICT)),
                                    storage.insert(Consumer.create("ЖМ-9", (byte) 0x15, SupportedConsumers.DISTRICT))
                            ),
                            Flux.merge(
                                    storage.insert(Consumer.create("П-1", (byte) 0x66, SupportedConsumers.INDUSTRY)),
                                    storage.insert(Consumer.create("П-2", (byte) 0x67, SupportedConsumers.INDUSTRY)),
                                    storage.insert(Consumer.create("П-3", (byte) 0x68, SupportedConsumers.INDUSTRY))
                            ),
                            Flux.merge(
                                    storage.insert(Consumer.create("С-1", (byte) 0x69, SupportedConsumers.HOSPITAL)),
                                    storage.insert(Consumer.create("С-2", (byte) 0x6A, SupportedConsumers.HOSPITAL)),
                                    storage.insert(Consumer.create("С-3", (byte) 0x6B, SupportedConsumers.HOSPITAL))
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

        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                prepareConfiguration();
            }
            catch (Exception ex) {
                logger.error("Ошибка подготовки элементов энергосети", ex);
            }
        });

    }
}
