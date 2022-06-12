package re.smartcity.sun;

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
import re.smartcity.wind.*;
import reactor.core.publisher.Mono;

@Component
public class SunRouterHandlers {

    private final Logger logger = LoggerFactory.getLogger(SunRouterHandlers.class);

    @Autowired
    private SunService sunService;

    @Autowired
    private SunStatusData sunStatusData;

    @Autowired
    private SunControlData sunControlData;

    @Autowired
    private ForecastStorage storage;

    public Mono<ServerResponse> setSunPower(ServerRequest rq) {
        logger.info("--> установить освещенность");

        Integer v = 0;
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

        sunControlData.addCommand(new WindControlCommand(WindControlCommands.POWER, v));

        return ServerResponse
                .ok()
                .header("Content-Language", "ru")
                .contentType(MediaType.TEXT_PLAIN)
                .body(Mono.just(String.format("освещенность: %s", v)), String.class);
    }

    public Mono<ServerResponse> sunOff(ServerRequest rq) {
        logger.info("--> освещение: отключить");

        sunControlData.addCommand(new WindControlCommand(WindControlCommands.ACTIVATE, false));

        return ServerResponse
                .ok()
                .header("Content-Language", "ru")
                .contentType(MediaType.TEXT_PLAIN)
                .body(Mono.just("отключить освещение"), String.class);
    }

    public Mono<ServerResponse> sunOn(ServerRequest rq) {
        logger.info("--> освещение: включить");

        sunControlData.addCommand(new WindControlCommand(WindControlCommands.ACTIVATE, true));

        return ServerResponse
                .ok()
                .header("Content-Language", "ru")
                .contentType(MediaType.TEXT_PLAIN)
                .body(Mono.just("включить освещение"), String.class);
    }

    public Mono<ServerResponse> getStatus(ServerRequest rq) {
        logger.info("--> освещение: статус");

        return ServerResponse
                .ok()
                .header("Content-Language", "ru")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(sunStatusData), WindStatusData.class);
    }

    // управление сервисом
    public Mono<ServerResponse> stopService(ServerRequest rq) {
        logger.info("--> задача управления солнцем: остановить");

        sunService.stop();

        return ServerResponse
                .ok()
                .header("Content-Language", "ru")
                .contentType(MediaType.TEXT_PLAIN)
                .body(Mono.just("задача управления солнцем: остановить"), String.class);
    }

    public Mono<ServerResponse> startService(ServerRequest rq) {
        logger.info("--> задача управления солнцем: запустить");

        sunService.start();

        return ServerResponse
                .ok()
                .header("Content-Language", "ru")
                .contentType(MediaType.TEXT_PLAIN)
                .body(Mono.just("задача управления солнцем: запустить"), String.class);
    }

    public Mono<ServerResponse> restartService(ServerRequest rq) {
        logger.info("--> задача управления солнцем: перезапустить");

        sunService.restart();

        return ServerResponse
                .ok()
                .header("Content-Language", "ru")
                .contentType(MediaType.TEXT_PLAIN)
                .body(Mono.just("задача управления солнцем: перезапустить"), String.class);
    }

    // работа с прогнозом
    public Mono<ServerResponse> forecastAll(ServerRequest rq) {
        logger.info("--> прогноз солнца: получить весь список");

        return ServerResponse
                .ok()
                .header("Content-Language", "ru")
                .contentType(MediaType.APPLICATION_JSON)
                .body(storage.findAll(ForecastTypes.SUN), Forecast.class);
    }

    public Mono<ServerResponse> forecastCreate(ServerRequest rq) {
        logger.info("--> прогноз солнца: создать");

        return ServerResponse
                .ok()
                .header("Content-Language", "ru")
                .contentType(MediaType.APPLICATION_JSON)
                .body(rq.bodyToMono(Forecast.class)
                        .flatMap(e -> {
                            e.setFc_type(ForecastTypes.SUN);
                            logger.info("--> тело запроса: {}", e);
                            return storage.create(e);
                        }), Forecast.class);
    }
}
