package re.smartcity.handlers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Component
public class SmartCityResourceHandler {

    private final Logger logger = LoggerFactory.getLogger(SmartCityResourceHandler.class);

    public Mono<ServerResponse> setWindPower(ServerRequest rq) {
        logger.info("--> установить силу ветра");

        String v = rq.pathVariable("value");
        logger.info("--> сила ветра: {}", v);

        return ServerResponse
                .ok()
                .header("Content-Language", "ru")
                .contentType(MediaType.TEXT_PLAIN)
                .body(Mono.just("сила ветра: " + v), String.class);
    }
}
