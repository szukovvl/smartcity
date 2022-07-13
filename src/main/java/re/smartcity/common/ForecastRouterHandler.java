package re.smartcity.common;

import org.apache.commons.math3.analysis.interpolation.AkimaSplineInterpolator;
import org.apache.commons.math3.analysis.polynomials.PolynomialSplineFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import re.smartcity.common.data.Forecast;
import re.smartcity.common.data.ForecastPoint;
import re.smartcity.common.data.ForecastTypes;
import re.smartcity.common.data.exchange.ForecastInterpolation;
import re.smartcity.energynet.component.data.client.SmallForecast;
import reactor.core.publisher.Mono;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.util.stream.Stream;

import static re.smartcity.common.resources.AppConstant.*;

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
                    .body(Mono.just("прогноз: неверный параметр"), String.class);
        }

        return ServerResponse
                .ok()
                .header("Content-Language", "ru")
                .contentType(MediaType.APPLICATION_JSON)
                .body(storage.findById(id), Forecast.class);
    }

    public Mono<ServerResponse> forecastUpdate(ServerRequest rq) {
        logger.info("--> прогноз: обновить данные");

        final Long id;
        try {
            id = Long.parseLong(rq.pathVariable("id"));
        }
        catch (NumberFormatException e) {
            return ServerResponse
                    .status(HttpStatus.NOT_IMPLEMENTED)
                    .header("Content-Language", "ru")
                    .contentType(MediaType.TEXT_PLAIN)
                    .body(Mono.just("прогноз: неверный параметр"), String.class);
        }

        return rq.bodyToMono(SmallForecast.class)
                        .flatMap(e -> {
                            return storage.findById(id)
                                    .flatMap(t -> {
                                        t.setName(e.getName());
                                        t.setData(e.getData());
                                        return storage.update(t);
                                    })
                                    .flatMap(t -> {
                                        return ServerResponse
                                                .ok()
                                                .header("Content-Language", "ru")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .bodyValue(t);
                                    })
                                    .onErrorResume(t -> {
                                        return ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                                .contentType(MediaType.TEXT_PLAIN)
                                                .bodyValue(t.getMessage());
                                    });
                        })
                .onErrorResume(t -> {
                    return ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .contentType(MediaType.TEXT_PLAIN)
                            .bodyValue(t.getMessage());
                });
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
                    .body(Mono.just("прогноз: неверный параметр"), String.class);
        }

        return ServerResponse
                .ok()
                .header("Content-Language", "ru")
                .contentType(MediaType.APPLICATION_JSON)
                .body(storage.remove(id), Integer.class);
    }

    public Mono<ServerResponse> forecastCreate(ServerRequest rq, ForecastTypes ftype) {
        return rq.bodyToMono(Forecast.class)
                .flatMap(e -> {
                    e.setFc_type(ftype);
                    return storage.create(e)
                            .flatMap(t -> {
                                return ServerResponse
                                        .ok()
                                        .header("Content-Language", "ru")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .bodyValue(t);
                            })
                            .onErrorResume(t -> {
                                return ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                        .contentType(MediaType.TEXT_PLAIN)
                                        .bodyValue(t.getMessage());
                            });
                });
    }

    public Mono<ServerResponse> interpolate(ServerRequest rq) {
        logger.info("--> прогноз: интерполяция");

        Long id = 0l;
        try {
            id = Long.parseLong(rq.pathVariable("id"));
        }
        catch (NumberFormatException e) {
            return ServerResponse
                    .status(HttpStatus.NOT_IMPLEMENTED)
                    .header("Content-Language", "ru")
                    .contentType(MediaType.TEXT_PLAIN)
                    .body(Mono.just("прогноз: неверный параметр"), String.class);
        }

        return storage.findById(id)
                .map(e -> {
                    ArrayList<ForecastPoint> wrkdata = new ArrayList<>(Arrays.stream(e.getData()).toList());
                    ForecastPoint[] data = e.getData();
                    if (data[0].getPoint().toSecondOfDay() != 0)
                    {
                        wrkdata.add(0, new ForecastPoint());
                    }
                    if (data[data.length - 1].getPoint().toSecondOfDay() < (GAMEDAY_MAX_MINUTES * 60)) {
                        ForecastPoint lastpt = new ForecastPoint();
                        lastpt.setPoint(LocalTime.ofSecondOfDay(GAMEDAY_MAX_MINUTES * 60L));
                        lastpt.setValue(data[data.length - 1].getValue());
                        wrkdata.add(lastpt);
                    }
                    ForecastInterpolation interpolation = new ForecastInterpolation();
                    if (wrkdata.size() < 5) {
                        interpolation.setLinear(true);
                        interpolation.setItems(wrkdata.toArray(ForecastPoint[]::new));
                    } else {
                        double[] xx = wrkdata.stream()
                                .mapToDouble(b -> ForecastPoint.TimeToDouble(b.getPoint())).toArray();
                        double[] yy = wrkdata.stream()
                                .mapToDouble(ForecastPoint::getValue).toArray();
                        PolynomialSplineFunction splineFunction = (new AkimaSplineInterpolator()).interpolate(xx, yy);
                        ForecastPoint[] pts = Stream.generate(ForecastPoint::new).limit(720).toArray(ForecastPoint[]::new);
                        long[] tm = { 0 };
                        Arrays.stream(pts).forEachOrdered(pt -> {
                            pt.setPoint(LocalTime.ofSecondOfDay(tm[0]));
                            tm[0] += 120;
                            double val = splineFunction.value(ForecastPoint.TimeToDouble(pt.getPoint()));
                            if (val < FORECAST_POINT_MIN_VALUE) {
                                val = FORECAST_POINT_MIN_VALUE;
                            } else if (val > FORECAST_POINT_MAX_VALUE) {
                                val = FORECAST_POINT_MAX_VALUE;
                            }
                            pt.setValue(val);
                        });
                        interpolation.setItems(pts);
                    }

                    return interpolation;
                })
                .flatMap(e -> {
                    return ServerResponse
                            .ok()
                            .header("Content-Language", "ru")
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(e);
                })
                .onErrorResume(t -> {
                    return ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .contentType(MediaType.TEXT_PLAIN)
                            .bodyValue(t.getMessage());
                });
    }

    public Mono<ServerResponse> randomize(ServerRequest rq) {
        logger.info("--> прогноз: случайное заполнение");

        Long id = 0l;
        try {
            id = Long.parseLong(rq.pathVariable("id"));
        }
        catch (NumberFormatException e) {
            return ServerResponse
                    .status(HttpStatus.NOT_IMPLEMENTED)
                    .header("Content-Language", "ru")
                    .contentType(MediaType.TEXT_PLAIN)
                    .body(Mono.just("прогноз: неверный параметр"), String.class);
        }

        return storage.findById(id)
                .flatMap(e -> {
                    ArrayList<ForecastPoint> data = new ArrayList<>();
                    final long[] tm = { 0 };
                    (new Random())
                            .doubles(0.0, 1.0)
                            .limit(48)
                            .forEachOrdered(v -> {
                                ForecastPoint pt = new ForecastPoint(LocalTime.ofSecondOfDay(tm[0] * 60), v);
                                tm[0] += 30;
                                data.add(pt);
                            });
                    e.setData(data.toArray(ForecastPoint[]::new));
                    storage.update(e);
                    return storage.update(e);
                })
                .flatMap(e -> {
                    return ServerResponse
                            .ok()
                            .header("Content-Language", "ru")
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(e);
                })
                .onErrorResume(t -> {
                    return ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .contentType(MediaType.TEXT_PLAIN)
                            .bodyValue(t.getMessage());
                });

    }
}
