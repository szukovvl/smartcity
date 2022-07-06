package re.smartcity.energynet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import re.smartcity.common.ForecastStorage;
import re.smartcity.common.data.Forecast;
import re.smartcity.common.data.ForecastTypes;
import re.smartcity.common.resources.Messages;
import re.smartcity.energynet.component.*;
import re.smartcity.energynet.component.data.*;
import re.smartcity.energynet.component.data.client.SmallConsumerSpecification;
import re.smartcity.modeling.ModelingData;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Stream;

@Component
public class EnergyRouterHandlers {

    private final Logger logger = LoggerFactory.getLogger(EnergyRouterHandlers.class);

    @Autowired
    private ModelingData modelingData;

    @Autowired
    EnergynetStorage storage;

    @Autowired
    ForecastStorage forecastStorage;

    public Mono<ServerResponse> find(ServerRequest rq) {

        SupportedTypes bytype;
        try {
            bytype = SupportedTypes.valueOf(rq.pathVariable("type").toUpperCase());
        }
        catch (Exception ex) {
            return ServerResponse
                    .status(HttpStatus.NOT_IMPLEMENTED)
                    .header("Content-Language", "ru")
                    .contentType(MediaType.TEXT_PLAIN)
                    .body(Mono.just("неверное значение типа объекта энергосети."), String.class);
        }

        if (bytype == SupportedTypes.MAINSUBSTATION) {
            return ServerResponse
                    .ok()
                    .header("Content-Language", "ru")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Mono.just(Arrays.stream(this.modelingData.getTasks())
                            .map(e -> e.getPowerSystem())
                            .toArray()), IComponentIdentification.class);
        }

        return ServerResponse
                .ok()
                .header("Content-Language", "ru")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(Arrays.stream(this.modelingData.getAllobjects())
                        .filter(e -> e.getComponentType() == bytype)
                        .toArray()), IComponentIdentification.class);
    }

    public Mono<ServerResponse> putData(ServerRequest rq) {

        String key = rq.pathVariable("key");

        // поиск объекта в данных модели
        Optional<IComponentIdentification> felement = Stream.concat(
                Arrays.stream(modelingData.getAllobjects()),
                Arrays.stream(modelingData.getTasks())
                        .map(e -> e.getPowerSystem()))
                .filter(e -> e.getIdenty().equals(key))
                .findFirst();

        if (felement.isEmpty()) {
            return ServerResponse
                    .status(HttpStatus.NOT_IMPLEMENTED)
                    .header("Content-Language", "ru")
                    .contentType(MediaType.TEXT_PLAIN)
                    .body(Mono.just("обновляемый объект не найден."), String.class);
        }

        IComponentIdentification memobj = felement.get();

        return rq.bodyToMono(SmallConsumerSpecification.class)
                .flatMap(rqobj -> {
                    IComponentManagement retobj = null;
                    try {
                        SmallConsumerSpecification.validate(rqobj);

                        // обновляем объект в данных модели и фиксируем в базе
                        switch (memobj.getComponentType()) {
                        /*case STORAGE: {
                            EnergyStorageSpecification lobj = ((EnergyStorage) memobj).getData();
                            lobj.setEnergy(rqobj.getEnergy());
                            retobj = lobj;
                            storage.updateData(key, lobj, EnergyStorage.class);
                            break;
                        }
                        case GENERATOR: {
                            GenerationSpecification lobj = ((Generation) memobj).getData();
                            lobj.setEnergy(rqobj.getEnergy());
                            retobj = lobj;
                            storage.updateData(key, lobj, Generation.class);
                            break;
                        }
                        case DISTRIBUTOR: {
                            EnergyDistributorSpecification lobj = ((EnergyDistributor) memobj).getData();
                            retobj = lobj;
                            storage.updateData(key, lobj, EnergyDistributor.class);
                            break;
                        }
                        case MAINSUBSTATION: {
                            MainSubstationSpecification lobj = ((MainSubstationPowerSystem) memobj).getData();
                            retobj = lobj;
                            storage.updateData(key, lobj, MainSubstationPowerSystem.class);
                            break;
                        }*/
                            case CONSUMER: {
                                ConsumerSpecification lobj = ((Consumer) memobj).getData();
                                lobj.setEnergy(rqobj.getEnergy());
                                lobj.setUseforecast(rqobj.isUseforecast());
                                lobj.setForecast(rqobj.getForecast());
                                retobj = lobj;
                                storage.updateData(key, lobj, Consumer.class)
                                        .map(ures -> {
                                            if (ures != 1) {
                                                logger.warn("обновляемый объект {} в хранилище не зафиксирован.", key);
                                            }
                                            return ures;
                                        })
                                        .subscribe();
                                break;
                            }
                        /*case GREEGENERATOR: {
                            GreenGenerationSpecification lobj = ((GreenGeneration) memobj).getData();
                            retobj = lobj;
                            storage.updateData(key, lobj, GreenGeneration.class);
                            break;
                        }*/
                            default: {
                                throw new IllegalArgumentException("неверный тип обновляемого объекта");
                            }
                        };
                    }
                    catch (IllegalArgumentException ex) {
                        return Mono.error(ex);
                    }

                    return ServerResponse
                            .ok()
                            .header("Content-Language", "ru")
                            .contentType(MediaType.APPLICATION_JSON)
                            .body(Mono.just(retobj), IComponentManagement.class);
                })
                .onErrorResume(t -> {
                    return ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .contentType(MediaType.TEXT_PLAIN)
                            .bodyValue(t.getMessage());
                })
                .switchIfEmpty(ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .contentType(MediaType.TEXT_PLAIN)
                        .bodyValue(Messages.ER_3));
    }

    public Mono<ServerResponse> forecast(ServerRequest rq) {
        ForecastTypes ftype;
        try {
            ftype = ForecastTypes.valueOf(rq.pathVariable("type").toUpperCase());
        }
        catch (Exception ex) {
            return ServerResponse
                    .status(HttpStatus.NOT_IMPLEMENTED)
                    .header("Content-Language", "ru")
                    .contentType(MediaType.TEXT_PLAIN)
                    .body(Mono.just("неверное значение типа прогноза."), String.class);
        }

        return ServerResponse
                .ok()
                .header("Content-Language", "ru")
                .contentType(MediaType.APPLICATION_JSON)
                .body(forecastStorage.findAll(ftype), Forecast.class);
    }
}
