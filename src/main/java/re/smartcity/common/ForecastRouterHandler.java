package re.smartcity.common;

import org.apache.commons.math3.analysis.interpolation.AkimaSplineInterpolator;
import org.apache.commons.math3.analysis.interpolation.LinearInterpolator;
import org.apache.commons.math3.analysis.polynomials.PolynomialSplineFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.r2dbc.support.ArrayUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import re.smartcity.common.data.Forecast;
import re.smartcity.common.data.ForecastPoint;
import re.smartcity.common.data.Point;
import reactor.core.publisher.Mono;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static re.smartcity.common.resources.AppConstant.FORECAST_POINT_MAX_VALUE;
import static re.smartcity.common.resources.AppConstant.FORECAST_POINT_MIN_VALUE;

@Component
public class ForecastRouterHandler {

    private final Logger logger = LoggerFactory.getLogger(ForecastRouterHandler.class);

    @Autowired
    private ForecastStorage storage;

    public Mono<ServerResponse> forecastById(ServerRequest rq) {
        logger.info("--> прогноз: получить прогноз по id");

        Long id = 0l;
        try {
            id = Long.parseLong(rq.pathVariable("id"));
        }
        catch (NumberFormatException e) {
            return ServerResponse
                    .status(HttpStatus.NOT_IMPLEMENTED)
                    .header("Content-Language", "ru")
                    .contentType(MediaType.TEXT_PLAIN)
                    .body(Mono.just("прогноз ветра: неверный параметр"), String.class);
        }

        return ServerResponse
                .ok()
                .header("Content-Language", "ru")
                .contentType(MediaType.APPLICATION_JSON)
                .body(storage.findById(id)
                        .map(e -> {
                            System.out.println();
                            Arrays.stream(e.getData()).forEachOrdered(a -> {
                                System.out.println(String.format("%s\t%s", a.getPoint().toSecondOfDay(), a.getValue() * 100.0));
                            });
                            System.out.println();
                            System.out.println();
                            Point[] pts = Stream.generate(() -> new Point()).limit(1440).toArray(Point[]::new);
                            final Integer[] tm = {0};
                            Arrays.stream(pts).forEachOrdered(a -> {
                                a.setX(tm[0].doubleValue());
                                a.setY(FORECAST_POINT_MIN_VALUE);
                                tm[0] += 60;
                            });


                            List<ForecastPoint> fpts = new ArrayList<ForecastPoint>();
                            fpts.addAll(Arrays.asList(e.getData()));
                            if (fpts.get(0).getPoint().toSecondOfDay() != 0)
                            {
                                fpts.add(0, new ForecastPoint(LocalTime.ofSecondOfDay(0), FORECAST_POINT_MIN_VALUE));
                            }
                            if (fpts.get(fpts.size() - 1).getPoint().toSecondOfDay() < pts[pts.length - 1].getX()) {
                                fpts.add(new ForecastPoint(LocalTime.ofSecondOfDay((long) pts[pts.length - 1].getX()), FORECAST_POINT_MIN_VALUE));
                            }

                            var xx = fpts.stream().mapToDouble(b -> (double) b.getPoint().toSecondOfDay()).toArray();
                            var yy = fpts.stream().mapToDouble(b -> b.getValue()).toArray();
                            final PolynomialSplineFunction[] funin = { null };
                            if (fpts.size() < 5) {
                                funin[0] = (new LinearInterpolator()).interpolate(xx, yy);
                            } else {
                                funin[0] = (new AkimaSplineInterpolator()).interpolate(xx, yy);
                            }

                            Arrays.stream(pts).forEachOrdered(b -> {
                                double val = funin[0].value(b.getX());
                                if (val < FORECAST_POINT_MIN_VALUE) {
                                    val = FORECAST_POINT_MIN_VALUE;
                                } else if (val > FORECAST_POINT_MAX_VALUE) {
                                    val = FORECAST_POINT_MAX_VALUE;
                                }
                                b.setY(val);
                                System.out.println(String.format("%s\t%s", b.getX(), b.getY() * 100.0));
                            });

                            System.out.println();
                            return e;
                        }), Forecast.class);
    }

    public Mono<ServerResponse> forecastUpdate(ServerRequest rq) {
        logger.info("--> прогноз: обновить данные");

        return ServerResponse
                .ok()
                .header("Content-Language", "ru")
                .contentType(MediaType.APPLICATION_JSON)
                .body(rq.bodyToMono(Forecast.class)
                        .flatMap(e -> {
                            logger.info("--> тело запроса: {}", e);
                            return storage.update(e);
                        }), Forecast.class);
    }

    public Mono<ServerResponse> forecastUpdatePoints(ServerRequest rq) {
        logger.info("--> прогноз: обновить данные точек");

        final Long id;
        try {
            id = Long.parseLong(rq.pathVariable("id"));
            logger.info("--> прогноз: {}", id);
        }
        catch (NumberFormatException e) {
            return ServerResponse
                    .status(HttpStatus.NOT_IMPLEMENTED)
                    .header("Content-Language", "ru")
                    .contentType(MediaType.TEXT_PLAIN)
                    .body(Mono.just("прогноз: обновить данные точек: неверный параметр"), String.class);
        }

        Mono<Forecast> res = rq.bodyToMono(String.class)
                .flatMap(e -> {
                    logger.info("--> обновляемый массив: {}", e);
                    return storage.updatePoints(id, e);
                })
                .flatMap(e -> {
                    if (e > 0) {
                        return storage.findById(id);
                    }
                    return null;
                });

        return ServerResponse
                .ok()
                .header("Content-Language", "ru")
                .contentType(MediaType.APPLICATION_JSON)
                .body(res, Forecast.class);
    }

    public Mono<ServerResponse> forecastRemove(ServerRequest rq) {
        logger.info("--> прогноз: удалить прогноз");

        Long id = 0l;
        try {
            id = Long.parseLong(rq.pathVariable("id"));
        }
        catch (NumberFormatException e) {
            return ServerResponse
                    .status(HttpStatus.NOT_IMPLEMENTED)
                    .header("Content-Language", "ru")
                    .contentType(MediaType.TEXT_PLAIN)
                    .body(Mono.just("прогноз ветра: неверный параметр"), String.class);
        }

        return ServerResponse
                .ok()
                .header("Content-Language", "ru")
                .contentType(MediaType.APPLICATION_JSON)
                .body(storage.remove(id), Integer.class);
    }
}
