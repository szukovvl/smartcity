package re.smartcity.handlers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import re.smartcity.data.TestBean;
import reactor.core.publisher.Mono;

@Component
public class SmartCityResourceHandler {

    private final Logger logger = LoggerFactory.getLogger(SmartCityResourceHandler.class);

    @Autowired
    private TestBean tst;
    @Autowired
    private TestBean tst1;


    public Mono<ServerResponse> setWindPower(ServerRequest rq) {
        logger.info("--> установить силу ветра");

        String v = rq.pathVariable("value");
        String old_v = tst.getMyVal();
        String old_v1 = tst1.getMyVal();
        logger.info("--> сила ветра: {} ({}; {})", v, old_v, old_v1);
        tst.setMyVal(v);

        return ServerResponse
                .ok()
                .header("Content-Language", "ru")
                .contentType(MediaType.TEXT_PLAIN)
                .body(Mono.just(String.format("сила ветра: %s (%s; %s)", v, old_v, old_v1)), String.class);
    }

    public Mono<ServerResponse> setSunPower(ServerRequest rq) {
        logger.info("--> установить освещенность");

        String v = rq.pathVariable("value");
        String old_v = tst.getMyVal();
        String old_v1 = tst1.getMyVal();
        logger.info("--> освещенность: {} ({}; {})", v, old_v, old_v1);
        tst.setMyVal(v);

        return ServerResponse
                .ok()
                .header("Content-Language", "ru")
                .contentType(MediaType.TEXT_PLAIN)
                .body(Mono.just(String.format("освещенность: %s (%s; %s)", v, old_v, old_v1)), String.class);
    }
}
