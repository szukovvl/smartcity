package re.smartcity.wind;

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
import reactor.core.publisher.Mono;

@Component
public class WindRouterHandlers {

    private final Logger logger = LoggerFactory.getLogger(WindRouterHandlers.class);

    @Autowired
    private WindService windService;

    @Autowired
    private WindStatusData windStatusData;

    @Autowired
    private WindControlData windControlData;

    @Autowired
    private ForecastStorage storage;

    public Mono<ServerResponse> setWindPower(ServerRequest rq) {
        logger.info("--> установить силу ветра");

        Integer v = 0;
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

        windControlData.addCommand(new WindControlCommand(WindControlCommands.POWER, v));

        return ServerResponse
                .ok()
                .header("Content-Language", "ru")
                .contentType(MediaType.TEXT_PLAIN)
                .body(Mono.just(String.format("сила ветра: %s", v)), String.class);
    }

    public Mono<ServerResponse> windOff(ServerRequest rq) {
        logger.info("--> вентилятор: отключить");

        windControlData.addCommand(new WindControlCommand(WindControlCommands.ACTIVATE, false));

        return ServerResponse
                .ok()
                .header("Content-Language", "ru")
                .contentType(MediaType.TEXT_PLAIN)
                .body(Mono.just("отключить вентилятор"), String.class);
    }

    public Mono<ServerResponse> windOn(ServerRequest rq) {
        logger.info("--> вентилятор: включить");

        windControlData.addCommand(new WindControlCommand(WindControlCommands.ACTIVATE, true));

        return ServerResponse
                .ok()
                .header("Content-Language", "ru")
                .contentType(MediaType.TEXT_PLAIN)
                .body(Mono.just("включить вентилятор"), String.class);
    }

    public Mono<ServerResponse> getStatus(ServerRequest rq) {
        logger.info("--> вентилятор: статус");

        return ServerResponse
                .ok()
                .header("Content-Language", "ru")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(windStatusData), WindStatusData.class);
    }

    // управление сервисом
    public Mono<ServerResponse> stopService(ServerRequest rq) {
        logger.info("--> задача управления ветром: остановить");

        windService.stop();

        return ServerResponse
                .ok()
                .header("Content-Language", "ru")
                .contentType(MediaType.TEXT_PLAIN)
                .body(Mono.just("задача управления ветром: остановить"), String.class);
    }

    public Mono<ServerResponse> startService(ServerRequest rq) {
        logger.info("--> задача управления ветром: запустить");

        windService.start();

        return ServerResponse
                .ok()
                .header("Content-Language", "ru")
                .contentType(MediaType.TEXT_PLAIN)
                .body(Mono.just("задача управления ветром: запустить"), String.class);
    }

    public Mono<ServerResponse> restartService(ServerRequest rq) {
        logger.info("--> задача управления ветром: перезапустить");

        windService.restart();

        return ServerResponse
                .ok()
                .header("Content-Language", "ru")
                .contentType(MediaType.TEXT_PLAIN)
                .body(Mono.just("задача управления ветром: перезапустить"), String.class);
    }

    // работа с прогнозом
    public Mono<ServerResponse> forecastAll(ServerRequest rq) {
        logger.info("--> прогноз ветра: получить весь список");

        return ServerResponse
                .ok()
                .header("Content-Language", "ru")
                .contentType(MediaType.APPLICATION_JSON)
                .body(storage.findAll(ForecastTypes.WIND), Forecast.class);
    }

    public Mono<ServerResponse> forecastById(ServerRequest rq) {
        logger.info("--> прогноз ветра: получить прогноз");

        Long id = 0l;
        try {
            id = Long.parseLong(rq.pathVariable("id"));
        }
        catch (NumberFormatException e) {
            return ServerResponse
                    .status(HttpStatus.NOT_IMPLEMENTED)
                    .header("Content-Language", "ru")
                    .contentType(MediaType.TEXT_PLAIN)
                    .body(Mono.just("прогноз ветра: неверный параметр"), String.class);
        }

        return ServerResponse
                .ok()
                .header("Content-Language", "ru")
                .contentType(MediaType.TEXT_PLAIN)
                .body(Mono.just(String.format("прогноз ветра: получить прогноз для %s", id)), String.class);
    }

    public Mono<ServerResponse> forecastUpdate(ServerRequest rq) {
        logger.info("--> прогноз ветра: обновить данные");

        return ServerResponse
                .ok()
                .header("Content-Language", "ru")
                .contentType(MediaType.TEXT_PLAIN)
                .body(Mono.just("прогноз ветра: обновить данные"), String.class);
    }

    public Mono<ServerResponse> forecastRemove(ServerRequest rq) {
        logger.info("--> прогноз ветра: удалить прогноз");

        Long id = 0l;
        try {
            id = Long.parseLong(rq.pathVariable("id"));
        }
        catch (NumberFormatException e) {
            return ServerResponse
                    .status(HttpStatus.NOT_IMPLEMENTED)
                    .header("Content-Language", "ru")
                    .contentType(MediaType.TEXT_PLAIN)
                    .body(Mono.just("прогноз ветра: неверный параметр"), String.class);
        }

        return ServerResponse
                .ok()
                .header("Content-Language", "ru")
                .contentType(MediaType.TEXT_PLAIN)
                .body(Mono.just(String.format("прогноз ветра: удалить прогноз %s", id)), String.class);
    }

    public Mono<ServerResponse> forecastCreate(ServerRequest rq) {
        logger.info("--> прогноз ветра: создать");

        return ServerResponse
                .ok()
                .header("Content-Language", "ru")
                .contentType(MediaType.TEXT_PLAIN)
                .body(Mono.just("прогноз ветра: создать"), String.class);
    }
}
