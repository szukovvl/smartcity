package re.smartcity.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import re.smartcity.common.data.exchange.MainInfoBlock;
import re.smartcity.stand.StandStatusData;
import re.smartcity.sun.SunStatusData;
import re.smartcity.wind.WindStatusData;
import reactor.core.publisher.Mono;

@Component
public class InfoRouterHandlers {

    private final Logger logger = LoggerFactory.getLogger(InfoRouterHandlers.class);

    @Autowired
    WindStatusData windStatus;

    @Autowired
    SunStatusData sunStatus;

    @Autowired
    StandStatusData standStatus;

    public Mono<ServerResponse> commonInfo(ServerRequest rq) {
        logger.info("--> информирование: общее состояние");

        MainInfoBlock res = new MainInfoBlock();
        res.setSunData(sunStatus);
        res.setWindData(windStatus);
        res.setStandStatus(standStatus);

        return ServerResponse
                .ok()
                .header("Content-Language", "ru")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(res), MainInfoBlock.class);
    }
}
