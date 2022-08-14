package re.smartcity.wind;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.util.UriComponentsBuilder;
import re.smartcity.common.CommonStorage;
import re.smartcity.common.ForecastRouterHandler;
import re.smartcity.common.ForecastStorage;
import re.smartcity.common.data.Forecast;
import re.smartcity.common.data.ForecastTypes;
import re.smartcity.common.data.exchange.SimpleWindData;
import re.smartcity.common.data.exchange.WindConfiguration;
import re.smartcity.common.utils.Helpers;
import re.smartcity.common.utils.Interpolation;
import reactor.core.publisher.Mono;

import static re.smartcity.common.resources.Messages.FER_0;

@Component
public class WindRouterHandlers {

    private final Logger logger = LoggerFactory.getLogger(WindRouterHandlers.class);

    @Autowired
    private CommonStorage commonStorage;

    @Autowired
    private WindStatusData windStatusData;

    @Autowired
    private WebClient windClient;

    @Autowired
    private ForecastStorage storage;

    @Autowired
    private ForecastRouterHandler forecastHandler;

    private void internalSetPower() {

        if (windStatusData.getUrl() == null || windStatusData.getUrl().equals("")) {
            windStatusData.setErrorMsg("Адрес сетевого ресурса устройста управления вентилятором не задан.");
            return;
        }

        windClient
                .get()
                .uri(UriComponentsBuilder
                        .fromHttpUrl(windStatusData.getUrl())
                        .path("Fan")
                        .queryParam("params", windStatusData.isOn() ? windStatusData.getPower() : 0)
                        .build()
                        .toUri())
                .exchangeToMono(response -> {
                    if (response.statusCode() == HttpStatus.OK) {

                        commonStorage.putData(WindConfiguration.key,
                                        new SimpleWindData(
                                                windStatusData.getPower(),
                                                windStatusData.getUrl(),
                                                windStatusData.getForecast(),
                                                windStatusData.isUseforecast()),
                                        WindConfiguration.class)
                                .map(res -> {
                                    if (res == 0) {
                                        logger.warn(FER_0, WindConfiguration.key);
                                    }
                                    return res;
                                })
                                .onErrorResume(t -> {
                                    logger.error(t.getMessage());
                                    return Mono.empty();
                                })
                                .subscribe();

                        windStatusData.setErrorMsg(null);
                    } else {
                        response.bodyToMono(String.class)
                                .map(msg -> {
                                    if (msg != null && !msg.equals("")) {
                                        windStatusData.setErrorMsg(String.format("Ошибка %d: %s", response.statusCode().value(), msg));
                                    } else {
                                        windStatusData.setErrorMsg(String.format("Ошибка %d", response.statusCode().value()));
                                    }

                                    return Mono.empty();
                                })
                                .onErrorResume(t -> {
                                    windStatusData.setErrorMsg(t.getMessage());
                                    return Mono.empty();
                                })
                                .subscribe();
                    }

                    return Mono.empty();
                })
                .onErrorResume(t -> {
                    windStatusData.setErrorMsg(t.getMessage());
                    return Mono.empty();
                })
                .subscribe();
    }

    private Mono<ServerResponse> internalSaveCfg() {

        return commonStorage.putData(WindConfiguration.key,
                        new SimpleWindData(
                                windStatusData.getPower(),
                                windStatusData.getUrl(),
                                windStatusData.getForecast(),
                                windStatusData.isUseforecast()),
                        WindConfiguration.class)
                .flatMap(count -> ServerResponse
                        .ok()
                        .header("Content-Language", "ru")
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(windStatusData))
                .onErrorResume(t -> ServerResponse
                        .status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .contentType(MediaType.TEXT_PLAIN)
                        .bodyValue(t.getMessage()));
    }

    public void internalSetOff() {
        windStatusData.setOn(false);
        internalSetPower();
    }

    public Mono<ServerResponse> setWindPower(ServerRequest rq) {
        int v;
        try {
            v = Integer.parseInt(rq.pathVariable("value"));
        }
        catch (NumberFormatException e) {
            return ServerResponse
                    .status(HttpStatus.NOT_IMPLEMENTED)
                    .header("Content-Language", "ru")
                    .contentType(MediaType.TEXT_PLAIN)
                    .body(Mono.just("сила ветра: неверный параметр"), String.class);
        }

        windStatusData.setPower(v);

        internalSetPower();

        return ServerResponse
                .ok()
                .header("Content-Language", "ru")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(windStatusData);
    }

    public Mono<ServerResponse> setWindURL(ServerRequest rq) {

        return rq.bodyToMono(SimpleWindData.class)
                        .flatMap(data -> {
                            windStatusData.setUrl(data.getUrl());

                            internalSetPower();

                            return ServerResponse
                                    .ok()
                                    .header("Content-Language", "ru")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .bodyValue(windStatusData);
                        })
                .onErrorResume(t -> ServerResponse
                        .status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .contentType(MediaType.TEXT_PLAIN)
                        .bodyValue(t.getMessage()));
    }

    public Mono<ServerResponse> windOff(ServerRequest ignoredRq) {

        windStatusData.setOn(false);

        internalSetPower();

        return ServerResponse
                .ok()
                .header("Content-Language", "ru")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(windStatusData);
    }

    public Mono<ServerResponse> windOn(ServerRequest ignoredRq) {

        windStatusData.setOn(true);

        internalSetPower();

        return ServerResponse
                .ok()
                .header("Content-Language", "ru")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(windStatusData);
    }

    public Mono<ServerResponse> windReconnect(ServerRequest ingnoredRq) {
        internalSetPower();

        return ServerResponse
                .ok()
                .header("Content-Language", "ru")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(windStatusData);
    }

    public Mono<ServerResponse> getStatus(ServerRequest ignoredRq) {
        return ServerResponse
                .ok()
                .header("Content-Language", "ru")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(windStatusData);
    }

    // работа с прогнозом
    public Mono<ServerResponse> forecastAll(ServerRequest ignoredRq) {
        return ServerResponse
                .ok()
                .header("Content-Language", "ru")
                .contentType(MediaType.APPLICATION_JSON)
                .body(storage.findAll(ForecastTypes.WIND), Forecast.class);
    }

    public Mono<ServerResponse> forecastCreate(ServerRequest rq) {
        return forecastHandler.forecastCreate(rq, ForecastTypes.WIND);
    }

    public Mono<ServerResponse> forecastUpdate(ServerRequest rq) {

        return rq.bodyToMono(SimpleWindData.class)
                .flatMap(body -> {
                    try {
                        SimpleWindData.validate(body);
                        if (body.getForecast() != null) {
                            body.getForecast().setData(Helpers.checkForecastBounds(body.getForecast().getData()));
                        }
                    }
                    catch (Exception ex) {
                        ServerResponse
                                .status(HttpStatus.NOT_IMPLEMENTED)
                                .header("Content-Language", "ru")
                                .contentType(MediaType.TEXT_PLAIN)
                                .body(Mono.just(ex.getMessage()), String.class);
                    }
                    windStatusData.setForecast(body.getForecast());
                    windStatusData.setUseforecast(body.isUseforecast());

                    return internalSaveCfg();
                });
    }

    public Mono<ServerResponse> interpolate(ServerRequest ignoredRq) {

        return ServerResponse
                .ok()
                .header("Content-Language", "ru")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Interpolation.interpolate(windStatusData.getForecast().getData(), 5.0));
    }
}
