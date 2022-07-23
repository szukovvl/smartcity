package re.smartcity.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.http.codec.multipart.Part;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyExtractors;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import re.smartcity.common.data.Forecast;
import re.smartcity.common.data.ForecastPoint;
import re.smartcity.common.data.ForecastTypes;
import re.smartcity.common.utils.Interpolation;
import re.smartcity.energynet.component.data.client.SmallForecast;
import reactor.core.publisher.Mono;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Map;
import java.util.Random;

@Component
public class ForecastRouterHandler {

    private final Logger logger = LoggerFactory.getLogger(ForecastRouterHandler.class);

    @Autowired
    private ForecastStorage storage;

    public Mono<ServerResponse> forecastById(ServerRequest rq) {
        long id;
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
        final long id;
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
                        .flatMap(e -> storage.findById(id)
                                .flatMap(t -> {
                                    t.setName(e.getName());
                                    t.setData(e.getData());
                                    return storage.update(t);
                                })
                                .flatMap(t -> ServerResponse
                                        .ok()
                                        .header("Content-Language", "ru")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .bodyValue(t))
                                .onErrorResume(t -> ServerResponse
                                        .status(HttpStatus.INTERNAL_SERVER_ERROR)
                                        .contentType(MediaType.TEXT_PLAIN)
                                        .bodyValue(t.getMessage())))
                .onErrorResume(t -> ServerResponse
                        .status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .contentType(MediaType.TEXT_PLAIN)
                        .bodyValue(t.getMessage()));
    }

    public Mono<ServerResponse> forecastUpdatePoints(ServerRequest rq) {
        final Long id;
        try {
            id = Long.parseLong(rq.pathVariable("id"));
        }
        catch (NumberFormatException e) {
            return ServerResponse
                    .status(HttpStatus.NOT_IMPLEMENTED)
                    .header("Content-Language", "ru")
                    .contentType(MediaType.TEXT_PLAIN)
                    .body(Mono.just("прогноз: обновить данные точек: неверный параметр"), String.class);
        }

        Mono<Forecast> res = rq.bodyToMono(String.class)
                .flatMap(e -> storage.updatePoints(id, e))
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
        long id;
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
                            .flatMap(t -> ServerResponse
                                    .ok()
                                    .header("Content-Language", "ru")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .bodyValue(t))
                            .onErrorResume(t -> ServerResponse
                                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                                    .contentType(MediaType.TEXT_PLAIN)
                                    .bodyValue(t.getMessage()));
                });
    }

    public Mono<ServerResponse> interpolate(ServerRequest rq) {
        long id;
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
                .map(e -> Interpolation.interpolate(e.getData()))
                .flatMap(e -> ServerResponse
                        .ok()
                        .header("Content-Language", "ru")
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(e))
                .onErrorResume(t -> ServerResponse
                        .status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .contentType(MediaType.TEXT_PLAIN)
                        .bodyValue(t.getMessage()));
    }

    public Mono<ServerResponse> randomize(ServerRequest rq) {
        long id;
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
                .flatMap(e -> ServerResponse
                        .ok()
                        .header("Content-Language", "ru")
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(e))
                .onErrorResume(t -> ServerResponse
                        .status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .contentType(MediaType.TEXT_PLAIN)
                        .bodyValue(t.getMessage()));

    }

    public Mono<ServerResponse> uploadFile(ServerRequest rq) {
        logger.info("--> загрузка файла");

        long id;
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
        logger.info("--> загрузка файла для {}", id);

        var res = rq.body(BodyExtractors.toMultipartData())
                .flatMap(parts -> {
                    Map<String, Part> partMap = parts.toSingleValueMap();

                    partMap.forEach((partName, value) -> logger.info("Name: {}, value: {}", partName, value));

                    FilePart image = (FilePart) partMap.get("myfile");
                    logger.info("File name: {}", image.filename());

                    image.content()
                            .map(DataBuffer::asInputStream)
                            .subscribe(e -> {
                                /*InputStreamReader reader = new InputStreamReader(e, StandardCharsets.UTF_8);
                                try {
                                    while (reader.ready()) {
                                        System.out.print((char) reader.read());
                                    }
                                }
                                catch (IOException ex) {
                                    logger.error(ex.getMessage());
                                }*/

                                try {
                                    ByteArrayOutputStream result = new ByteArrayOutputStream();
                                    byte[] buffer = new byte[1024];
                                    int length;
                                    while ((length = e.read(buffer)) != -1) {
                                        result.write(buffer, 0, length);
                                    }

                                    System.out.println(result.toString(StandardCharsets.UTF_8));
                                }
                                catch (IOException ex) {
                                    logger.error(ex.getMessage());
                                }
                            });

                    return Mono.just("Ok!");
                });

        return ServerResponse
                .ok()
                .header("Content-Language", "ru")
                .contentType(MediaType.TEXT_PLAIN)
                .body(res, String.class);
    }
}
