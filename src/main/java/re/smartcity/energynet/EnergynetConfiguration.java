package re.smartcity.energynet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import re.smartcity.energynet.component.*;
import re.smartcity.modeling.ModelingData;
import re.smartcity.stand.SerialElementAddresses;
import reactor.core.publisher.Flux;

import javax.annotation.PostConstruct;
import java.util.Arrays;
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
                            storage.insert(MainSubstationPowerSystem
                                    .create("Гп-1",
                                            SerialElementAddresses.MAIN_SUBSTATION_1,
                                            new byte[] {
                                                    SerialElementAddresses.MAIN_SUBSTATION_1_CONNECTOR_2,
                                                    SerialElementAddresses.MAIN_SUBSTATION_1_CONNECTOR_3,
                                                    SerialElementAddresses.MAIN_SUBSTATION_1_CONNECTOR_4
                                            },
                                            new byte[] {
                                                    SerialElementAddresses.MAIN_SUBSTATION_1_CONNECTOR_5,
                                                    SerialElementAddresses.MAIN_SUBSTATION_1_CONNECTOR_6,
                                                    SerialElementAddresses.MAIN_SUBSTATION_1_CONNECTOR_7
                                            })),
                            storage.insert(MainSubstationPowerSystem
                                    .create("Гп-2",
                                            SerialElementAddresses.MAIN_SUBSTATION_2,
                                            new byte[] {
                                                    SerialElementAddresses.MAIN_SUBSTATION_2_CONNECTOR_2,
                                                    SerialElementAddresses.MAIN_SUBSTATION_2_CONNECTOR_3,
                                                    SerialElementAddresses.MAIN_SUBSTATION_2_CONNECTOR_4
                                            },
                                            new byte[] {
                                                    SerialElementAddresses.MAIN_SUBSTATION_2_CONNECTOR_5,
                                                    SerialElementAddresses.MAIN_SUBSTATION_2_CONNECTOR_6,
                                                    SerialElementAddresses.MAIN_SUBSTATION_2_CONNECTOR_7
                                            }))
                    )
                    .toStream()
                    .toArray(MainSubstationPowerSystem[]::new);

            elements = Flux.merge(
                            Flux.merge(
                                    storage.insert(EnergyDistributor
                                            .create("Мп-1",
                                                    SerialElementAddresses.MINI_SUBSTATION_1,
                                                    SerialElementAddresses.MINI_SUBSTATION_1_CONNECTOR_1,
                                                    new byte[] {
                                                            SerialElementAddresses.MINI_SUBSTATION_1_CONNECTOR_2,
                                                            SerialElementAddresses.MINI_SUBSTATION_1_CONNECTOR_3
                                                    })),
                                    storage.insert(EnergyDistributor
                                            .create("Мп-2",
                                                    SerialElementAddresses.MINI_SUBSTATION_2,
                                                    SerialElementAddresses.MINI_SUBSTATION_2_CONNECTOR_1,
                                                    new byte[] {
                                                            SerialElementAddresses.MINI_SUBSTATION_2_CONNECTOR_2,
                                                            SerialElementAddresses.MINI_SUBSTATION_2_CONNECTOR_3
                                                    }))
                            ),
                            Flux.merge(
                                    storage.insert(Generation
                                            .create("Дг-1", SerialElementAddresses.DIESEL_GENERATOR_1)),
                                    storage.insert(Generation
                                            .create("Дг-2", SerialElementAddresses.DIESEL_GENERATOR_2)),
                                    storage.insert(Generation
                                            .create("Дг-3", SerialElementAddresses.DIESEL_GENERATOR_3)),
                                    storage.insert(Generation
                                            .create("Дг-4", SerialElementAddresses.DIESEL_GENERATOR_4)),
                                    storage.insert(Generation
                                            .create("Дг-5", SerialElementAddresses.DIESEL_GENERATOR_5))
                            ),
                            Flux.merge(
                                    storage.insert(GreenGeneration
                                            .create("Сг-1", SerialElementAddresses.SOLAR_BATTERY_1, SupportedGenerations.SOLAR)),
                                    storage.insert(GreenGeneration
                                            .create("Сг-2", SerialElementAddresses.SOLAR_BATTERY_2, SupportedGenerations.SOLAR)),
                                    storage.insert(GreenGeneration
                                            .create("Сг-3", SerialElementAddresses.SOLAR_BATTERY_3, SupportedGenerations.SOLAR)),
                                    storage.insert(GreenGeneration
                                            .create("Сг-4", SerialElementAddresses.SOLAR_BATTERY_4, SupportedGenerations.SOLAR)),
                                    storage.insert(GreenGeneration
                                            .create("Сг-5", SerialElementAddresses.SOLAR_BATTERY_5, SupportedGenerations.SOLAR))
                            ),
                            Flux.merge(
                                    storage.insert(GreenGeneration
                                            .create("Вг-1", SerialElementAddresses.WIND_GENERATOR_1, SupportedGenerations.WIND)),
                                    storage.insert(GreenGeneration
                                            .create("Вг-2", SerialElementAddresses.WIND_GENERATOR_2, SupportedGenerations.WIND)),
                                    storage.insert(GreenGeneration
                                            .create("Вг-3", SerialElementAddresses.WIND_GENERATOR_3, SupportedGenerations.WIND))
                            ),
                            Flux.merge(
                                    storage.insert(EnergyStorage
                                            .create("ЭХакб-1", SerialElementAddresses.BATTERY_1)),
                                    storage.insert(EnergyStorage
                                            .create("ЭХакб-2", SerialElementAddresses.BATTERY_2)),
                                    storage.insert(EnergyStorage
                                            .create("ЭХакб-3", SerialElementAddresses.BATTERY_3)),
                                    storage.insert(EnergyStorage
                                            .create("ЭХакб-4", SerialElementAddresses.BATTERY_4)),
                                    storage.insert(EnergyStorage
                                            .create("ЭХакб-5", SerialElementAddresses.BATTERY_5))
                            ),
                            Flux.merge(
                                    storage.insert(Consumer
                                            .create("ЖМ-1",
                                                    SerialElementAddresses.DISTRICT_1,
                                                    new byte[] {
                                                            SerialElementAddresses.DISTRICT_1
                                                    },
                                                    SupportedConsumers.DISTRICT)),
                                    storage.insert(Consumer
                                            .create("ЖМ-2",
                                                    SerialElementAddresses.DISTRICT_2,
                                                    new byte[] {
                                                            SerialElementAddresses.DISTRICT_2
                                                    },
                                                    SupportedConsumers.DISTRICT)),
                                    storage.insert(Consumer
                                            .create("ЖМ-3",
                                                    SerialElementAddresses.DISTRICT_3,
                                                    new byte[] {
                                                            SerialElementAddresses.DISTRICT_3
                                                    },
                                                    SupportedConsumers.DISTRICT)),
                                    storage.insert(Consumer
                                            .create("ЖМ-4",
                                                    SerialElementAddresses.DISTRICT_4,
                                                    new byte[] {
                                                            SerialElementAddresses.DISTRICT_4
                                                    },
                                                    SupportedConsumers.DISTRICT)),
                                    storage.insert(Consumer
                                            .create("ЖМ-5",
                                                    SerialElementAddresses.DISTRICT_5,
                                                    new byte[] {
                                                            SerialElementAddresses.DISTRICT_5
                                                    },
                                                    SupportedConsumers.DISTRICT)),
                                    storage.insert(Consumer
                                            .create("ЖМ-6",
                                                    SerialElementAddresses.DISTRICT_6,
                                                    new byte[] {
                                                            SerialElementAddresses.DISTRICT_6
                                                    },
                                                    SupportedConsumers.DISTRICT)),
                                    storage.insert(Consumer
                                            .create("ЖМ-7",
                                                    SerialElementAddresses.DISTRICT_7,
                                                    new byte[] {
                                                            SerialElementAddresses.DISTRICT_7
                                                    },
                                                    SupportedConsumers.DISTRICT)),
                                    storage.insert(Consumer
                                            .create("ЖМ-8",
                                                    SerialElementAddresses.DISTRICT_8,
                                                    new byte[] {
                                                            SerialElementAddresses.DISTRICT_8
                                                    },
                                                    SupportedConsumers.DISTRICT)),
                                    storage.insert(Consumer
                                            .create("ЖМ-9",
                                                    SerialElementAddresses.DISTRICT_9,
                                                    new byte[] {
                                                            SerialElementAddresses.DISTRICT_9
                                                    },
                                                    SupportedConsumers.DISTRICT))
                            ),
                            Flux.merge(
                                    storage.insert(Consumer
                                            .create("П-1",
                                                    SerialElementAddresses.FACTORY_1,
                                                    new byte[] {
                                                            SerialElementAddresses.FACTORY_1_CONNECTOR_1,
                                                            SerialElementAddresses.FACTORY_1_CONNECTOR_2
                                                    },
                                                    SupportedConsumers.INDUSTRY)),
                                    storage.insert(Consumer
                                            .create("П-2",
                                                    SerialElementAddresses.FACTORY_2,
                                                    new byte[] {
                                                            SerialElementAddresses.FACTORY_2_CONNECTOR_1,
                                                            SerialElementAddresses.FACTORY_2_CONNECTOR_2
                                                    },
                                                    SupportedConsumers.INDUSTRY)),
                                    storage.insert(Consumer
                                            .create("П-3",
                                                    SerialElementAddresses.FACTORY_3,
                                                    new byte[] {
                                                            SerialElementAddresses.FACTORY_3_CONNECTOR_1,
                                                            SerialElementAddresses.FACTORY_3_CONNECTOR_2
                                                    },
                                                    SupportedConsumers.INDUSTRY))
                            ),
                            Flux.merge(
                                    storage.insert(Consumer
                                            .create("С-1",
                                                    SerialElementAddresses.HOSPITAL_1,
                                                    new byte[] {
                                                            SerialElementAddresses.HOSPITAL_1_CONNECTOR_1,
                                                            SerialElementAddresses.HOSPITAL_1_CONNECTOR_2
                                                    },
                                                    SupportedConsumers.HOSPITAL)),
                                    storage.insert(Consumer
                                            .create("С-2",
                                                    SerialElementAddresses.HOSPITAL_2,
                                                    new byte[] {
                                                            SerialElementAddresses.HOSPITAL_2_CONNECTOR_1,
                                                            SerialElementAddresses.HOSPITAL_2_CONNECTOR_2
                                                    },
                                                    SupportedConsumers.HOSPITAL)),
                                    storage.insert(Consumer
                                            .create("С-3",
                                                    SerialElementAddresses.HOSPITAL_3,
                                                    new byte[] {
                                                            SerialElementAddresses.HOSPITAL_3_CONNECTOR_1,
                                                            SerialElementAddresses.HOSPITAL_3_CONNECTOR_2
                                                    },
                                                    SupportedConsumers.HOSPITAL))
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
        model.putOnMonitoring(mainSubstations, Arrays.stream(elements)
                .filter(e -> e.getComponentType() == SupportedTypes.DISTRIBUTOR)
                .toArray(EnergyDistributor[]::new));
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
