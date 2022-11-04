package re.smartcity.energynet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import re.smartcity.common.ForecastRouterHandler;
import re.smartcity.common.ForecastStorage;
import re.smartcity.common.data.Forecast;
import re.smartcity.common.data.ForecastTypes;
import re.smartcity.common.resources.Messages;
import re.smartcity.common.utils.Helpers;
import re.smartcity.common.utils.Interpolation;
import re.smartcity.energynet.component.*;
import re.smartcity.energynet.component.data.*;
import re.smartcity.energynet.component.data.client.*;
import re.smartcity.modeling.ModelingData;
import re.smartcity.modeling.TaskData;
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

    @Autowired
    private ForecastRouterHandler forecastHandler;

    private Class<?> getObjectScpecification(SupportedTypes st) throws IllegalArgumentException {
        return switch (st) {
            case CONSUMER -> SmallConsumerSpecification.class;
            case GENERATOR -> SmallGenerationSpecification.class;
            case STORAGE -> SmallStorageSpecification.class;
            case GREEGENERATOR -> SmallGreenGenerationSpecification.class;
            case DISTRIBUTOR -> SmallSubnetSpecification[].class;
            case MAINSUBSTATION -> SmallMainStationSpecification.class;
            default -> throw new IllegalArgumentException(Messages.ER_8);
        };
    }

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
                            .map(TaskData::getPowerSystem)
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
                        .map(TaskData::getPowerSystem))
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

        return rq.bodyToMono(getObjectScpecification(memobj.getComponentType()))
                .flatMap(rqobj -> {
                    ISpecifications retobj;
                    Class<?> clazz;
                    try {
                        // обновляем объект в данных модели и фиксируем в базе
                        switch (memobj.getComponentType()) {
                            case STORAGE -> {
                                SmallStorageSpecification sss = (SmallStorageSpecification) rqobj;
                                SmallStorageSpecification.validate(sss);
                                EnergyStorageSpecification lobj = ((EnergyStorage) memobj).getData();
                                SmallStorageSpecification.AssignTo(sss, lobj);
                                clazz = EnergyStorage.class;
                                retobj = lobj;
                            }
                            case GENERATOR -> {
                                SmallGenerationSpecification sgs = (SmallGenerationSpecification) rqobj;
                                SmallGenerationSpecification.validate(sgs);
                                if (sgs.getForecast() != null) {
                                    sgs.getForecast().setData(Helpers.checkForecastBounds(sgs.getForecast().getData()));
                                }
                                GenerationSpecification lobj = ((Generation) memobj).getData();
                                SmallGenerationSpecification.AssignTo(sgs, lobj);
                                clazz = Generation.class;
                                retobj = lobj;
                            }
                            case DISTRIBUTOR -> {
                                SmallSubnetSpecification[] sss = Arrays.stream((SmallSubnetSpecification[]) rqobj)
                                        .peek(SmallSubnetSpecification::validate)
                                        .toArray(SmallSubnetSpecification[]::new);

                                EnergyDistributorSpecification lobj = ((EnergyDistributor) memobj).getData();
                                for (int i = 0; i < sss.length; i++) {
                                    SmallSubnetSpecification.AssignTo(sss[i], lobj.getOutputs()[i].getData());
                                }

                                clazz = EnergyDistributor.class;
                                retobj = lobj;
                            }
                            case MAINSUBSTATION -> {
                                SmallMainStationSpecification smss = (SmallMainStationSpecification) rqobj;
                                SmallMainStationSpecification.validate(smss);
                                MainSubstationSpecification lobj = ((MainSubstationPowerSystem) memobj).getData();
                                SmallMainStationSpecification.AssignTo(smss, lobj);

                                clazz = MainSubstationPowerSystem.class;
                                retobj = lobj;
                            }
                            case CONSUMER -> {
                                SmallConsumerSpecification scs = (SmallConsumerSpecification) rqobj;
                                SmallConsumerSpecification.validate(scs);
                                if (scs.getForecast() != null) {
                                    scs.getForecast().setData(Helpers.checkForecastBounds(scs.getForecast().getData()));
                                }
                                ConsumerSpecification lobj = ((Consumer) memobj).getData();
                                SmallConsumerSpecification.AssignTo(scs, lobj);
                                clazz = Consumer.class;
                                retobj = lobj;
                            }
                            case GREEGENERATOR -> {
                                SmallGreenGenerationSpecification sggs = (SmallGreenGenerationSpecification) rqobj;
                                SmallGreenGenerationSpecification.validate(sggs);
                                GreenGenerationSpecification lobj = ((GreenGeneration) memobj).getData();
                                SmallGreenGenerationSpecification.AssignTo(sggs, lobj);
                                clazz = GreenGeneration.class;
                                retobj = lobj;
                            }
                            default -> throw new IllegalArgumentException("неверный тип обновляемого объекта");
                        }
                        storage.updateData(key, retobj, clazz)
                                .map(ures -> {
                                    if (ures != 1) {
                                        logger.warn(Messages.FER_0, key);
                                    }
                                    return ures;
                                })
                                .subscribe();
                    }
                    catch (IllegalArgumentException ex) {
                        return Mono.error(ex);
                    }

                    return ServerResponse
                            .ok()
                            .header("Content-Language", "ru")
                            .contentType(MediaType.APPLICATION_JSON)
                            .body(Mono.just(retobj), retobj.getClass());
                })
                .onErrorResume(t -> ServerResponse
                        .status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .contentType(MediaType.TEXT_PLAIN)
                        .bodyValue(t.getMessage()))
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

    public Mono<ServerResponse> forecastCreate(ServerRequest rq) {
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

        return forecastHandler.forecastCreate(rq, ftype);
    }

    public Mono<ServerResponse> interpolate(ServerRequest rq) {

        String key = rq.pathVariable("key");

        // поиск объекта в данных модели
        Optional<IComponentIdentification> felement = Stream.concat(
                        Arrays.stream(modelingData.getAllobjects()),
                        Arrays.stream(modelingData.getTasks())
                                .map(TaskData::getPowerSystem))
                .filter(e -> e.getIdenty().equals(key))
                .findFirst();

        if (felement.isEmpty()) {
            return ServerResponse
                    .status(HttpStatus.NOT_IMPLEMENTED)
                    .header("Content-Language", "ru")
                    .contentType(MediaType.TEXT_PLAIN)
                    .body(Mono.just("объект не найден."), String.class);
        }

        IComponentIdentification memobj = felement.get();
        double scale;
        Forecast forecast;
        switch (memobj.getComponentType()) {
            case CONSUMER -> {
                forecast = ((Consumer) memobj).getData().getForecast();
                scale = ((Consumer) memobj).getData().getEnergy();
            }
            case GENERATOR -> {
                forecast = ((Generation) memobj).getData().getForecast();
                scale = ((Generation) memobj).getData().getEnergy();
            }
            default -> {
                return ServerResponse
                        .status(HttpStatus.NOT_IMPLEMENTED)
                        .header("Content-Language", "ru")
                        .contentType(MediaType.TEXT_PLAIN)
                        .body(Mono.just("не поддерживаемый тип объекта."), String.class);
            }
        }

        if (forecast == null) {
            return ServerResponse
                    .status(HttpStatus.NOT_IMPLEMENTED)
                    .header("Content-Language", "ru")
                    .contentType(MediaType.TEXT_PLAIN)
                    .body(Mono.just("прогноз не установлен."), String.class);
        }

        return ServerResponse
                .ok()
                .header("Content-Language", "ru")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Interpolation.interpolate(forecast.getData(), scale));
    }
}
