package re.smartcity.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;
import re.smartcity.wind.WindRouterHandlers;

import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
public class ResourceRouter {

    // управление вентилятором
    @Bean
    public RouterFunction<ServerResponse> WindRouterFunction(WindRouterHandlers handler) {
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
                    builder.GET("/forecast/find/{id}", handler::forecastById);
                    builder.PUT("/forecast/data", handler::forecastUpdate);
                    builder.DELETE("/forecast/{id}", handler::forecastRemove);
                    builder.POST("/forecast", handler::forecastCreate);
                }
        ).build();
    }
}
