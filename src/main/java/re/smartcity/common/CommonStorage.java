package re.smartcity.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.data.relational.core.query.Query;
import org.springframework.data.relational.core.query.Update;
import org.springframework.stereotype.Component;
import re.smartcity.common.resources.Messages;
import reactor.core.publisher.Mono;

@Component
public class CommonStorage {

    private final Logger logger = LoggerFactory.getLogger(CommonStorage.class);

    @Autowired
    private R2dbcEntityTemplate template;

    public <T> Mono<T> create(String key, Class<T> clazz) {
        try {
            return template.insert(clazz.getConstructor((Class<?>[]) null).newInstance((Object[]) null));
        }
        catch (Exception ex) {
            logger.warn(Messages.FER_1, key);
            return Mono.error(ex);
        }
    }

    public <T> Mono<T> getAndCreate(String key, Class<T> clazz) {
        return template.selectOne(Query
                .query(Criteria.where("id").is(key))
                        .columns("data"), clazz)
                .switchIfEmpty(create(key, clazz));
    }

    public <T, V> Mono<Integer> putData(String key, T data, Class<V> clazz) {
        return template.update(Query
                        .query(Criteria.where("id").is(key)),
                Update.update("data", data), clazz);
    }
}
