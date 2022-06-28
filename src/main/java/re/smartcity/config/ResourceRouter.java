package re.smartcity.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;
import re.smartcity.common.ForecastRouterHandler;
import re.smartcity.common.InfoRouterHandlers;
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
                    builder.PUT("/{value}", handler::setWindPower);
                    builder.POST("/off", handler::windOff);
                    builder.POST("/on", handler::windOn);

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
                    builder.PUT("", handler::forecastUpdate);
                    builder.PUT("/data/{id}", handler::forecastUpdatePoints);
                    builder.DELETE("/{id}", handler::forecastRemove);
                }
        ).build();
    }

    // информирование
    @Bean
    public RouterFunction<ServerResponse> infoRouterFunction(InfoRouterHandlers handler) {
        return route().nest(
                RequestPredicates.path("/api/1_0/common"),
                builder -> {

                    // прогноз
                    builder.GET("", handler::commonInfo);
                }
        ).build();
    }

}
