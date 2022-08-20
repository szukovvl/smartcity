package re.smartcity.config.sockets;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.HandlerMapping;
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping;
import org.springframework.web.reactive.socket.server.support.WebSocketHandlerAdapter;

import java.util.HashMap;
import java.util.Map;

import static re.smartcity.common.resources.AppConstant.SOCKET_COMMON_SERVICE;
import static re.smartcity.common.resources.AppConstant.SOCKET_GAME_SERVICE;

@Configuration
public class SocketConfiguration {

    @Autowired
    private CommonSocketHandler commonSocketHandler;

    @Autowired
    private GameSocketHandler gameSocketHandler;

    @Bean
    public HandlerMapping webSocketMapping() {
        Map<String, Object> map = new HashMap<>();
        map.put(SOCKET_COMMON_SERVICE, commonSocketHandler);
        map.put(SOCKET_GAME_SERVICE, gameSocketHandler);
        SimpleUrlHandlerMapping simpleUrlHandlerMapping = new SimpleUrlHandlerMapping();
        simpleUrlHandlerMapping.setUrlMap(map);

        simpleUrlHandlerMapping.setOrder(10);
        return simpleUrlHandlerMapping;
    }

    @Bean
    public WebSocketHandlerAdapter handlerAdapter() {
        return new WebSocketHandlerAdapter();
    }
}
