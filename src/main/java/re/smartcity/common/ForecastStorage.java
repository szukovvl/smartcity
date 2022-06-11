package re.smartcity.common;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.data.relational.core.query.CriteriaDefinition;
import org.springframework.data.relational.core.query.Query;
import org.springframework.stereotype.Component;
import re.smartcity.common.data.Forecast;
import re.smartcity.common.data.ForecastTypes;
import reactor.core.publisher.Flux;

@Component
public class ForecastStorage {

    @Autowired
    private R2dbcEntityTemplate template;

    public Flux<Forecast> findAll(ForecastTypes fc) {
        return this.template
                .select(Query
                        .query(Criteria.where("fc_type").is(fc))
                        .sort(Sort.by("name")),
                        Forecast.class);
    }
}
