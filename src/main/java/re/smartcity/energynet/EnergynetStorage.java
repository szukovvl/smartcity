package re.smartcity.energynet;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.data.relational.core.query.Query;
import org.springframework.stereotype.Component;
import re.smartcity.energynet.component.MainSubstationPowerSystem;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
public class EnergynetStorage {

    @Autowired
    private R2dbcEntityTemplate template;

    public Flux<IComponentIdentification> find(SupportedTypes stype) {
        return template.select(Query
                .query(Criteria.where("componenttype").is(stype))
                .sort(Sort.by("identy")), MainSubstationPowerSystem.class)
                .map(e -> e);
    }

    public <T> Mono<T> insert(T entity) {
        return template.insert(entity);
    }
}
