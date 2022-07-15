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
import re.smartcity.common.data.exchange.ForecastInterpolation;
import re.smartcity.common.resources.Messages;
import re.smartcity.common.utils.Interpolation;
import re.smartcity.energynet.component.*;
import re.smartcity.energynet.component.data.*;
import re.smartcity.energynet.component.data.client.SmallConsumerSpecification;
import re.smartcity.modeling.ModelingData;
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
            Double val = e.getValue();
            if (val < FORECAST_POINT_MIN_VALUE) e.setValue(FORECAST_POINT_MIN_VALUE);
            if (val > FORECAST_POINT_MAX_VALUE) e.setValue(FORECAST_POINT_MAX_VALUE);
        });

        return items;
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
                        if (rqobj.getForecast() != null)
                        {
                            rqobj.getForecast().setData(checkForecastBounds(rqobj.getForecast().getData()));
                        }

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
                                SmallConsumerSpecification.AssignTo(rqobj, lobj);
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
                                .map(e -> e.getPowerSystem()))
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
        double scale = 1.0;
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
