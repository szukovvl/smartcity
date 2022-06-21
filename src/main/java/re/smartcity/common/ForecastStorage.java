package re.smartcity.common;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.data.relational.core.query.Query;
import org.springframework.data.relational.core.query.Update;
import org.springframework.stereotype.Component;
import re.smartcity.common.data.Forecast;
import re.smartcity.common.data.ForecastPoint;
import re.smartcity.common.data.ForecastTypes;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Arrays;

import static re.smartcity.common.resources.AppConstant.FORECAST_POINT_MAX_VALUE;
import static re.smartcity.common.resources.AppConstant.FORECAST_POINT_MIN_VALUE;

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

    /*
    перед вставкой и обновлением необходимо проверить массив:
     - отсортировать по возрастанию,
     - исключить дубликаты,
     - проверить верхние и нижние границы.
     - ниличие, как минимум, одной точки
     */
    public ForecastPoint[] validatePoints(ForecastPoint[] pts) {
        if (pts == null || pts.length == 0) {
            throw new IllegalArgumentException("Прогноз должен содержать как минимум одну точку.");
        }
        if (pts.length == 1) {
            if (pts[0].getValue() < FORECAST_POINT_MIN_VALUE) pts[0].setValue(FORECAST_POINT_MIN_VALUE);
            if (pts[0].getValue() > FORECAST_POINT_MAX_VALUE) pts[0].setValue(FORECAST_POINT_MAX_VALUE);
            return pts;
        }

        //var s = Arrays.stream(pts).distinct().sorted();
        var s = Arrays.stream(pts)
                .sorted()
                .distinct();
        pts = s.toArray(ForecastPoint[]::new);

        Arrays.stream(pts).forEachOrdered(e ->
        {
            Double val = e.getValue();
            if (val < FORECAST_POINT_MIN_VALUE) e.setValue(FORECAST_POINT_MIN_VALUE);
            if (val > FORECAST_POINT_MAX_VALUE) e.setValue(FORECAST_POINT_MAX_VALUE);
        });

        return pts;
    }

    public Mono<Forecast> create(Forecast v) {
        return this.template.insert(v);
    }

    public Mono<Forecast> findById(Long id) {
        return this.template.selectOne(Query.query(Criteria.where("id").is(id)), Forecast.class);
    }

    public Mono<Forecast> update(Forecast v) {
        return this.template.update(v);
    }

    public Mono<Integer> remove(Long id) {
        return this.template.delete(Query.query(Criteria.where("id").is(id)), Forecast.class);
    }

    public Mono<Integer> updatePoints(Long id, String body) {
        try{
            body = new ObjectMapper().writeValueAsString(
                    validatePoints(new ObjectMapper().readValue(body, ForecastPoint[].class)));
        }
        catch (JsonProcessingException ex) {
            return Mono.error(ex);
        }
        return this.template.update(Query.query(Criteria.where("id").is(id)),
                Update.update("data", body),
                Forecast.class);
    }
}
