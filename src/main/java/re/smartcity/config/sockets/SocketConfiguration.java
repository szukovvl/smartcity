package re.smartcity.config.sockets;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.HandlerMapping;
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping;
import org.springframework.web.reactive.socket.server.support.WebSocketHandlerAdapter;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.util.HashMap;
import java.util.Map;

import static re.smartcity.common.resources.AppConstant.SOCKET_COMMON_SERVICE;

@Configuration
public class SocketConfiguration {

    @Bean
    public Sinks.Many<CommonServiceEvent<?>> eventPublisher() {

        return Sinks
                .many()
                .replay()
                .latest();
    }

    @Bean
    public Flux<CommonServiceEvent<?>> events(Sinks.Many<CommonServiceEvent<?>> eventPublisher) {
        return eventPublisher
                .asFlux()
                .replay(1)
                .autoConnect();
    }

    @Bean
    public HandlerMapping webSocketMapping(CommonSocketHandler handler) {
        Map<String, Object> map = new HashMap<>();
        map.put(SOCKET_COMMON_SERVICE, handler);
        SimpleUrlHandlerMapping simpleUrlHandlerMapping = new SimpleUrlHandlerMapping();
        simpleUrlHandlerMapping.setUrlMap(map);

        simpleUrlHandlerMapping.setOrder(10);
        return simpleUrlHandlerMapping;
    }
    /*@Bean
    public HandlerMapping webSocketMapping(Sinks.Many<CommonServiceEvent<?>> eventPublisher, Flux<CommonServiceEvent<?>> events) {
        Map<String, Object> map = new HashMap<>();
        map.put(SOCKET_COMMON_SERVICE, new CommonSocketHandler(eventPublisher, events));
        SimpleUrlHandlerMapping simpleUrlHandlerMapping = new SimpleUrlHandlerMapping();
        simpleUrlHandlerMapping.setUrlMap(map);

        simpleUrlHandlerMapping.setOrder(10);
        return simpleUrlHandlerMapping;
    }*/

    @Bean
    public WebSocketHandlerAdapter handlerAdapter() {
        return new WebSocketHandlerAdapter();
    }
}
