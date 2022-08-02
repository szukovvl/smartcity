package re.smartcity.sun;

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
import re.smartcity.wind.*;
import reactor.core.publisher.Mono;

@Component
public class SunRouterHandlers {

    @Autowired
    private SunService sunService;

    @Autowired
    private SunStatusData sunStatusData;

    @Autowired
    private SunControlData sunControlData;

    @Autowired
    private ForecastStorage storage;

    @Autowired
    private ForecastRouterHandler forecastHandler;

    public Mono<ServerResponse> setSunPower(ServerRequest rq) {
        int v;
        try {
            v = Integer.parseInt(rq.pathVariable("value"));
        }
        catch (NumberFormatException e) {
            return ServerResponse
                    .status(HttpStatus.NOT_IMPLEMENTED)
                    .header("Content-Language", "ru")
                    .contentType(MediaType.TEXT_PLAIN)
                    .body(Mono.just("освещенность: неверный параметр"), String.class);
        }

        sunControlData.addCommand(new SunControlCommand(SunControlCommands.POWER, v));

        return ServerResponse
                .ok()
                .header("Content-Language", "ru")
                .contentType(MediaType.TEXT_PLAIN)
                .body(Mono.just(String.format("освещенность: %s", v)), String.class);
    }

    public Mono<ServerResponse> sunOff(ServerRequest rq) {
        sunControlData.addCommand(new SunControlCommand(SunControlCommands.ACTIVATE, false));

        return ServerResponse
                .ok()
                .header("Content-Language", "ru")
                .contentType(MediaType.TEXT_PLAIN)
                .body(Mono.just("отключить освещение"), String.class);
    }

    public Mono<ServerResponse> sunOn(ServerRequest rq) {
        sunControlData.addCommand(new SunControlCommand(SunControlCommands.ACTIVATE, true));

        return ServerResponse
                .ok()
                .header("Content-Language", "ru")
                .contentType(MediaType.TEXT_PLAIN)
                .body(Mono.just("включить освещение"), String.class);
    }

    public Mono<ServerResponse> getStatus(ServerRequest rq) {
        return ServerResponse
                .ok()
                .header("Content-Language", "ru")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(sunStatusData), WindStatusData.class);
    }

    // управление сервисом
    public Mono<ServerResponse> stopService(ServerRequest rq) {
        sunService.stop();

        return ServerResponse
                .ok()
                .header("Content-Language", "ru")
                .contentType(MediaType.TEXT_PLAIN)
                .body(Mono.just("задача управления солнцем: остановить"), String.class);
    }

    public Mono<ServerResponse> startService(ServerRequest rq) {
        sunService.start();

        return ServerResponse
                .ok()
                .header("Content-Language", "ru")
                .contentType(MediaType.TEXT_PLAIN)
                .body(Mono.just("задача управления солнцем: запустить"), String.class);
    }

    public Mono<ServerResponse> restartService(ServerRequest rq) {
        sunService.restart();

        return ServerResponse
                .ok()
                .header("Content-Language", "ru")
                .contentType(MediaType.TEXT_PLAIN)
                .body(Mono.just("задача управления солнцем: перезапустить"), String.class);
    }

    // работа с прогнозом
    public Mono<ServerResponse> forecastAll(ServerRequest rq) {
        return ServerResponse
                .ok()
                .header("Content-Language", "ru")
                .contentType(MediaType.APPLICATION_JSON)
                .body(storage.findAll(ForecastTypes.SUN), Forecast.class);
    }

    public Mono<ServerResponse> forecastCreate(ServerRequest rq) {
        return forecastHandler.forecastCreate(rq, ForecastTypes.SUN);
    }
}
