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
import re.smartcity.common.ForecastRouterHandler;
import re.smartcity.common.ForecastStorage;
import re.smartcity.common.data.Forecast;
import re.smartcity.common.data.ForecastTypes;
import reactor.core.publisher.Mono;

import java.net.MalformedURLException;

@Component
public class WindRouterHandlers {

    private final Logger logger = LoggerFactory.getLogger(WindRouterHandlers.class);

    @Autowired
    private WindStatusData windStatusData;

    @Autowired
    private WebClient windClient;

    @Autowired
    private ForecastStorage storage;

    @Autowired
    private ForecastRouterHandler forecastHandler;

    private void internalSetPower() {

        logger.info(UriComponentsBuilder
                .fromHttpUrl(windStatusData.getUrl())
                .path("Fan")
                .queryParam("params", windStatusData.isOn() ? windStatusData.getPower() : 0)
                .build()
                .toUri()
                .toASCIIString());

        if (windStatusData.getUrl() == null || windStatusData.getUrl().equals("")) {
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
                                .subscribe();
                    }

                    return Mono.empty();
                })
                .subscribe();
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

        windStatusData.setUrl(rq.pathVariable("value"));

        internalSetPower();

        return ServerResponse
                .ok()
                .header("Content-Language", "ru")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(windStatusData);
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
        return forecastHandler.forecastCreate(rq, ForecastTypes.SUN);
    }
}
