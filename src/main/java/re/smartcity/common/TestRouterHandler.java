package re.smartcity.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.data.relational.core.query.Query;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import re.smartcity.energynet.IComponentIdentification;
import re.smartcity.energynet.SupportedTypes;
import re.smartcity.energynet.component.ConsumerA;
import re.smartcity.energynet.component.GenerationA;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
public class TestRouterHandler {

    private final Logger logger = LoggerFactory.getLogger(TestRouterHandler.class);

    @Autowired
    private R2dbcEntityTemplate template;

    public Mono<ServerResponse> getCmp(ServerRequest rq) {
        logger.info("--> чтение компонент");

        Flux<ConsumerA> res1 = template.select(Query
                .query(Criteria.where("componenttype").is(SupportedTypes.CONSUMER)), ConsumerA.class);
        Flux<GenerationA> res2 = template.select(Query
                .query(Criteria.where("componenttype").is(SupportedTypes.GENERATOR)), GenerationA.class);

        return ServerResponse
                .ok()
                .header("Content-Language", "ru")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Flux.merge(res1, res2), IComponentIdentification.class);
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
