package re.smartcity.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.data.relational.core.query.CriteriaDefinition;
import org.springframework.data.relational.core.query.Query;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import re.smartcity.common.data.Forecast;
import re.smartcity.energynet.IComponentIdentification;
import re.smartcity.energynet.component.ConsumerA;
import re.smartcity.energynet.component.GenerationA;
import re.smartcity.wind.WindControlCommand;
import re.smartcity.wind.WindControlCommands;
import re.smartcity.wind.WindRouterHandlers;
import re.smartcity.wind.WindStatusData;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
public class TestRouterHandler {

    private final Logger logger = LoggerFactory.getLogger(TestRouterHandler.class);

    @Autowired
    private R2dbcEntityTemplate template;

    public Mono<ServerResponse> getCmp(ServerRequest rq) {
        logger.info("--> чтение компонент");

        return ServerResponse
                .ok()
                .header("Content-Language", "ru")
                .contentType(MediaType.TEXT_PLAIN)
                .body(Mono.just("не решил пока..."), String.class);
    }

    public Mono<ServerResponse> updateCmp(ServerRequest rq) {
        logger.info("--> записать компонент");

        ConsumerA cmp1 = ConsumerA.create("C1");
        GenerationA cmp2 = GenerationA.create("G1");

        Flux<IComponentIdentification> res = Flux.merge(this.template.insert(cmp1), this.template.insert(cmp2));

        return ServerResponse
                .ok()
                .header("Content-Language", "ru")
                .contentType(MediaType.APPLICATION_JSON)
                .body(res, IComponentIdentification.class);
    }
}
