package re.smartcity.energynet;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.data.relational.core.query.Query;
import org.springframework.data.relational.core.query.Update;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
public class EnergynetStorage {

    @Autowired
    private R2dbcEntityTemplate template;

    public <T> Flux<IComponentIdentification> find(SupportedTypes stype, Class<T> clazz) {
        return template.select(Query
                        .query(Criteria.where("componentType").is(stype))
                        .columns("id", "identy", "devaddr", "data")
                        .sort(Sort.by("identy")), clazz)
                .map(e -> (IComponentIdentification) e);
    }

    public <T> Mono<T> insert(T entity) {
        return template.insert(entity);
    }

    public <T, V> Mono<Integer> updateData(String key, V data, Class<T> clazz) {
        return template.update(Query
                        .query(Criteria.where("identy").is(key)),
                Update.update("data", data), clazz);
    }
}
