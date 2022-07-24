package re.smartcity.common;

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
import re.smartcity.common.data.exchange.ForecastUploadPoints;
import re.smartcity.common.utils.Interpolation;
import re.smartcity.energynet.component.data.client.SmallForecast;
import reactor.core.publisher.Mono;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class ForecastRouterHandler {

    // private final Logger logger = LoggerFactory.getLogger(ForecastRouterHandler.class);

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
                                ForecastPoint pt = new ForecastPoint(
                                        LocalTime.ofSecondOfDay(tm[0] * 60),
                                        Math.round(v * 100.0) / 100.0);
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

        return rq.body(BodyExtractors.toMultipartData())
                .flatMap(parts -> {
                    ForecastUploadPoints ret_data = new ForecastUploadPoints();
                    Map<String, Part> partMap = parts.toSingleValueMap();

                    FilePart image = (FilePart) partMap.get("points_file");
                    if (image == null) {
                        ret_data.setErrormsg("запрос не содержит данных.");
                        return Mono.just(ret_data);
                    }

                    return image
                            .content()
                            .map(DataBuffer::asInputStream)
                            .map(inputStream -> {
                                List<ForecastPoint> points = new ArrayList<>();
                                ByteArrayOutputStream result = new ByteArrayOutputStream();
                                byte[] buffer = new byte[1024];
                                int length;
                                try {
                                    while ((length = inputStream.read(buffer)) != -1) {
                                        result.write(buffer, 0, length);
                                    }
                                }
                                catch (IOException ex) {
                                    ret_data.setErrormsg(ex.getMessage());
                                    return ret_data;
                                }

                                String text = result.toString(StandardCharsets.UTF_8);
                                Pattern text_pattern = Pattern.compile("(.+?)\\r");
                                Pattern line_pattern = Pattern.compile("^(\\S+?)\\t(\\S+?)$");
                                Matcher matcher = text_pattern.matcher(text);
                                int pat_count = 0;
                                int err_count = 0;

                                while (matcher.find()) {
                                    Matcher line = line_pattern.matcher(matcher.group(1));
                                    if (line.find()) {
                                        pat_count++;
                                        try {
                                            points.add(new ForecastPoint(
                                                    LocalTime.parse(line.group(1)),
                                                    Double.parseDouble(line.group(2).replace(',', '.'))));
                                        }
                                        catch (NumberFormatException ex) {
                                            err_count++;
                                        }
                                    }
                                }

                                if (points.size() == 0)
                                {
                                    ret_data.setErrormsg("данные не соотвествуют шаблону или отсутствуют.");
                                    return ret_data;
                                } else if ((err_count * 100) / pat_count > 50) {
                                    ret_data.setErrormsg("данные содержат слишком много ошибок.");
                                    return ret_data;
                                } else if (err_count != 0) {
                                    ret_data.setErrormsg("преобразование выполнено с ошибками.");
                                }

                                ret_data.setPoints(points.toArray(ForecastPoint[]::new));
                                ret_data.setInterpolation(Interpolation.interpolate(ret_data.getPoints()));

                                return ret_data;
                            })
                            .single()
                            .flatMap(e -> {
                                if (id < 0) {
                                    return Mono.just(1);
                                }
                                if (e.getPoints().length != 0) {
                                    return storage.updatePoints(id, e.getPoints());
                                }
                                return Mono.just(-1);
                            })
                            .map(e -> {
                                if (e == 0) {
                                    ret_data.setErrormsg("синхронизация с хранилищем не выполнена.");
                                }
                                return ret_data;
                            });
                })
                .flatMap(res -> ServerResponse
                        .ok()
                        .header("Content-Language", "ru")
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(res))
                .onErrorResume(t -> ServerResponse
                        .status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .contentType(MediaType.TEXT_PLAIN)
                        .bodyValue(t.getMessage()));
    }

    public Mono<ServerResponse> exportPoints(ServerRequest rq) {
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
                .map(Forecast::getData)
                .flatMap(points -> {
                    StringBuilder text = new StringBuilder();
                    Arrays.stream(points).forEachOrdered(pt -> {
                        text.append(pt.getPoint());
                        text.append("\t");
                        text.append(pt.getValue());
                        text.append("\r\n");
                    });

                    return ServerResponse
                            .ok()
                            .header("Content-Language", "ru")
                            .header("Content-Disposition", "attachment; filename=\"data.txt\"")
                            .contentType(MediaType.TEXT_PLAIN)
                            .bodyValue(text.toString());
                })
                .onErrorResume(t -> ServerResponse
                        .status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .contentType(MediaType.TEXT_PLAIN)
                        .bodyValue(t.getMessage()));
    }
}
