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
import re.smartcity.stand.StandRouterHandlers;
import re.smartcity.sun.SunRouterHandlers;
import re.smartcity.wind.WindRouterHandlers;

import static org.springframework.web.reactive.function.server.RouterFunctions.route;
import static re.smartcity.common.resources.AppConstant.*;

@Configuration
public class ResourceRouter {

    // управление вентилятором
    @Bean
    public RouterFunction<ServerResponse> windRouterFunction(WindRouterHandlers handler) {
        return route().nest(
                RequestPredicates.path(API_WIND_SERVICE),
                builder -> {
                    builder.GET("", handler::getStatus);
                    builder.PUT("/power/{value}", handler::setWindPower);
                    builder.PUT("/url", handler::setWindURL);
                    builder.POST("/off", handler::windOff);
                    builder.POST("/on", handler::windOn);
                    builder.POST("/reconnect", handler::windReconnect);

                    // прогноз
                    builder.GET("/forecast/all", handler::forecastAll);
                    builder.POST("/forecast", handler::forecastCreate);
                    builder.PUT("/forecast", handler::forecastUpdate);

                    // разное
                    builder.GET("/interpolate", handler::interpolate);
                }
        ).build();
    }

    // управление осветителями
    @Bean
    public RouterFunction<ServerResponse> sunRouterFunction(SunRouterHandlers handler) {
        return route().nest(
                RequestPredicates.path(API_SUN_SERVICE),
                builder -> {
                    builder.GET("", handler::getStatus);
                    builder.PUT("/power/{value}", handler::setSunPower);
                    builder.POST("/off", handler::sunOff);
                    builder.POST("/on", handler::sunOn);

                    // прогноз
                    builder.GET("/forecast/all", handler::forecastAll);
                    builder.POST("/forecast", handler::forecastCreate);
                    builder.PUT("/forecast", handler::forecastUpdate);

                    // разное
                    builder.GET("/interpolate", handler::interpolate);
                }
        ).build();
    }

    // стенд
    @Bean
    public RouterFunction<ServerResponse> standRouterFunction(StandRouterHandlers handler) {
        return route().nest(
                RequestPredicates.path(API_STAND_SERVICE),
                builder -> {
                    builder.GET("", handler::getStatus);
                    builder.GET("/control", handler::getControl);
                    builder.PUT("/control", handler::putControl);
                    builder.GET("/ports", handler::getPortNames);

                    // управление сервисом
                    builder.PUT("/service/stop", handler::stopService);
                    builder.PUT("/service/start", handler::startService);
                    builder.PUT("/service/restart", handler::restartService);
                }
        ).build();
    }

    // прогнозы
    @Bean
    public RouterFunction<ServerResponse> forecastRouterFunction(ForecastRouterHandler handler) {
        return route().nest(
                RequestPredicates.path(API_FORECAST_SERVICE),
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
                RequestPredicates.path(API_COMMON_SERVICE),
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
                RequestPredicates.path(API_ENERGY_SERVICE),
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
