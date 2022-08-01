package re.smartcity.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;
import re.smartcity.common.ForecastRouterHandler;
import re.smartcity.common.InfoRouterHandlers;
import re.smartcity.energynet.EnergyRouterHandlers;
import re.smartcity.sun.SunRouterHandlers;
import re.smartcity.wind.WindRouterHandlers;

import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
public class ResourceRouter {

    // управление вентилятором
    @Bean
    public RouterFunction<ServerResponse> windRouterFunction(WindRouterHandlers handler) {
        return route().nest(
                RequestPredicates.path("/api/1_0/wind"),
                builder -> {
                    builder.GET("", handler::getStatus);
                    builder.PUT("/power/{value}", handler::setWindPower);
                    builder.PUT("/url/{value}", handler::setWindURL);
                    builder.POST("/off", handler::windOff);
                    builder.POST("/on", handler::windOn);

                    // прогноз
                    builder.GET("/forecast/all", handler::forecastAll);
                    builder.POST("/forecast", handler::forecastCreate);
                }
        ).build();
    }

    // управление осветителями
    @Bean
    public RouterFunction<ServerResponse> sunRouterFunction(SunRouterHandlers handler) {
        return route().nest(
                RequestPredicates.path("/api/1_0/sun"),
                builder -> {
                    builder.GET("", handler::getStatus);
                    builder.PUT("/{value}", handler::setSunPower);
                    builder.POST("/off", handler::sunOff);
                    builder.POST("/on", handler::sunOn);

                    // управление сервисом
                    builder.PUT("/service/stop", handler::stopService);
                    builder.PUT("/service/start", handler::startService);
                    builder.PUT("/service/restart", handler::restartService);

                    // прогноз
                    builder.GET("/forecast/all", handler::forecastAll);
                    builder.POST("/forecast", handler::forecastCreate);
                }
        ).build();
    }

    // прогнозы
    @Bean
    public RouterFunction<ServerResponse> forecastRouterFunction(ForecastRouterHandler handler) {
        return route().nest(
                RequestPredicates.path("/api/1_0/forecast"),
                builder -> {

                    // прогноз
                    builder.GET("/{id}", handler::forecastById);
                    builder.PUT("/{id}", handler::forecastUpdate);
                    builder.PUT("/data/{id}", handler::forecastUpdatePoints);
                    builder.DELETE("/{id}", handler::forecastRemove);
                    builder.GET("/interpolate/{id}", handler::interpolate);
                    builder.GET("/random/{id}", handler::randomize);

                    builder.POST("/upload/{id}",
                            RequestPredicates.contentType(MediaType.MULTIPART_FORM_DATA),
                            handler::uploadFile);
                    builder.GET("/export/{id}", handler::exportPoints);

                }
        ).build();
    }

    // информирование и прочее
    @Bean
    public RouterFunction<ServerResponse> infoRouterFunction(InfoRouterHandlers handler) {
        return route().nest(
                RequestPredicates.path("/api/1_0/common"),
                builder -> {

                    // обобщенное состояние
                    builder.GET("", handler::commonInfo);

                    // тарифы
                    builder.GET("/tariffs", handler::getTariffs);
                    builder.PUT("/tariffs", handler::putTariffs);

                    // критерии оценки игры
                    builder.GET("/criteria", handler::getCriteria);
                    builder.PUT("/criteria", handler::putCriteria);
                }
        ).build();
    }

    // объекты энергосистемы
    @Bean
    public RouterFunction<ServerResponse> energoRouterFunction(EnergyRouterHandlers handler) {
        return route().nest(
                RequestPredicates.path("/api/1_0/energy"),
                builder -> {

                    // работа с объектами
                    builder.GET("/find/{type}", handler::find);
                    builder.PUT("/data/{key}", handler::putData);

                    // прогноз
                    builder.GET("/forecast/{type}", handler::forecast);
                    builder.POST("/forecast/{type}", handler::forecastCreate);

                    // разное
                    builder.GET("/interpolate/{key}", handler::interpolate);
                }
        ).build();
    }

}
