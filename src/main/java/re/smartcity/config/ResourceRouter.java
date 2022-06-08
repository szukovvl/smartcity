package re.smartcity.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;
import re.smartcity.handlers.SmartCityResourceHandler;

import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
public class ResourceRouter {

    @Bean
    public RouterFunction<ServerResponse> smRouterFunction(SmartCityResourceHandler handler) {
        return route().nest(
                RequestPredicates.path("/api/1_0"),
                builder -> {
                    builder.PUT("/wind/{value}", handler::setWindPower);
                }
        ).build();
    }

}
