package re.smartcity.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import re.smartcity.common.data.Forecast;
import reactor.core.publisher.Mono;

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
                        /*.map(e -> {
                            Point[] pts = Stream.generate(() -> new Point()).limit(1440).toArray(Point[]::new);
                            //Point[] pts = Stream.generate(() -> new Point()).limit(1000).toArray(Point[]::new);
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

                            System.out.println();
                            fpts.forEach(a -> {
                                System.out.println(String.format("%.2f\t%.2f", ForecastPoint.TimeToDouble(a.getPoint()), a.getValue() * 100.0));
                            });
                            System.out.println();
                            System.out.println();

                            var xx = fpts.stream().mapToDouble(b -> ForecastPoint.TimeToDouble(b.getPoint())).toArray();
                            var yy = fpts.stream().mapToDouble(b -> b.getValue()).toArray();
                            final PolynomialSplineFunction[] funin = { null };

                            if (fpts.size() < 5) {
                                funin[0] = (new LinearInterpolator()).interpolate(xx, yy);
                            } else {
                                funin[0] = (new AkimaSplineInterpolator()).interpolate(xx, yy);
                            }

                            Arrays.stream(pts).forEachOrdered(b -> {
                                double vx = ForecastPoint.TimeToDouble(
                                        LocalTime.ofSecondOfDay((long) b.getX()));
                                double val = funin[0].value(vx);
                                if (val < FORECAST_POINT_MIN_VALUE) {
                                    val = FORECAST_POINT_MIN_VALUE;
                                } else if (val > FORECAST_POINT_MAX_VALUE) {
                                    val = FORECAST_POINT_MAX_VALUE;
                                }
                                b.setY(val);
                                System.out.println(String.format("%.2f\t%.2f",
                                        vx, b.getY() * 100.0));
                            });

                            System.out.println();
                            return e;
                        })*/, Forecast.class);
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
