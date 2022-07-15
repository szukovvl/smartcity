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
import re.smartcity.common.data.ForecastPoint;
import re.smartcity.common.data.ForecastTypes;
import re.smartcity.common.resources.Messages;
import re.smartcity.common.utils.Interpolation;
import re.smartcity.energynet.component.*;
import re.smartcity.energynet.component.data.*;
import re.smartcity.energynet.component.data.client.SmallConsumerSpecification;
import re.smartcity.energynet.component.data.client.SmallGenerationSpecification;
import re.smartcity.modeling.ModelingData;
import re.smartcity.modeling.TaskData;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Stream;

import static re.smartcity.common.resources.AppConstant.FORECAST_POINT_MAX_VALUE;
import static re.smartcity.common.resources.AppConstant.FORECAST_POINT_MIN_VALUE;

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

    private ForecastPoint[] checkForecastBounds(ForecastPoint[] items) {
        if (items == null || items.length == 0) {
            return items;
        }
        var s = Arrays.stream(items)
                .sorted()
                .distinct();
        items = s.toArray(ForecastPoint[]::new);

        Arrays.stream(items).forEachOrdered(e ->
        {
            double val = e.getValue();
            if (val < FORECAST_POINT_MIN_VALUE) e.setValue(FORECAST_POINT_MIN_VALUE);
            if (val > FORECAST_POINT_MAX_VALUE) e.setValue(FORECAST_POINT_MAX_VALUE);
        });

        return items;
    }

    private Class getObjectScpecification(SupportedTypes st) throws IllegalArgumentException {
        switch (st) {
            case CONSUMER: return SmallConsumerSpecification.class;
            case GENERATOR: return SmallGenerationSpecification.class;
            default: throw new IllegalArgumentException(Messages.ER_8);
        }
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
                    IComponentManagement retobj;
                    try {
                        // обновляем объект в данных модели и фиксируем в базе
                        switch (memobj.getComponentType()) {
                        /*case STORAGE: {
                            EnergyStorageSpecification lobj = ((EnergyStorage) memobj).getData();
                            lobj.setEnergy(rqobj.getEnergy());
                            retobj = lobj;
                            storage.updateData(key, lobj, EnergyStorage.class);
                            break;
                        }*/
                            case GENERATOR: {
                                SmallGenerationSpecification sgs = (SmallGenerationSpecification) rqobj;
                                SmallGenerationSpecification.validate(sgs);
                                if (sgs.getForecast() != null)
                                {
                                    sgs.getForecast().setData(checkForecastBounds(sgs.getForecast().getData()));
                                }
                                GenerationSpecification lobj = ((Generation) memobj).getData();
                                SmallGenerationSpecification.AssignTo(sgs, lobj);
                                retobj = lobj;
                                storage.updateData(key, lobj, Generation.class)
                                        .map(ures -> {
                                            if (ures != 1) {
                                                logger.warn(Messages.FER_0, key);
                                            }
                                            return ures;
                                        })
                                        .subscribe();
                                break;
                            }
                        /*case DISTRIBUTOR: {
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
                                SmallConsumerSpecification scs = (SmallConsumerSpecification) rqobj;
                                SmallConsumerSpecification.validate(scs);
                                if (scs.getForecast() != null)
                                {
                                    scs.getForecast().setData(checkForecastBounds(scs.getForecast().getData()));
                                }
                                ConsumerSpecification lobj = ((Consumer) memobj).getData();
                                SmallConsumerSpecification.AssignTo(scs, lobj);
                                retobj = lobj;
                                storage.updateData(key, lobj, Consumer.class)
                                        .map(ures -> {
                                            if (ures != 1) {
                                                logger.warn(Messages.FER_0, key);
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
                        }
                    }
                    catch (IllegalArgumentException ex) {
                        return Mono.error(ex);
                    }

                    return ServerResponse
                            .ok()
                            .header("Content-Language", "ru")
                            .contentType(MediaType.APPLICATION_JSON)
                            .body(Mono.just(retobj), IComponentManagement.class);
                }).
                onErrorResume(t -> {
                    return ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .contentType(MediaType.TEXT_PLAIN)
                            .bodyValue(((Throwable) t).getMessage());
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
            case CONSUMER: {
                forecast = ((Consumer) memobj).getData().getForecast();
                scale = ((Consumer) memobj).getData().getEnergy();
                break;
            }
            case GENERATOR: {
                forecast = ((Generation) memobj).getData().getForecast();
                scale = ((Generation) memobj).getData().getEnergy();
                break;
            }
            default: {
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
