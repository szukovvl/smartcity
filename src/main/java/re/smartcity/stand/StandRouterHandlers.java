package re.smartcity.stand;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Component
public class StandRouterHandlers {

    @Autowired
    private StandStatusData standStatusData;

    @Autowired
    private StandService standService;

    public Mono<ServerResponse> getStatus(ServerRequest ignoredRq) {
        return ServerResponse
                .ok()
                .header("Content-Language", "ru")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(standStatusData);
    }

    // управление сервисом
    public Mono<ServerResponse> stopService(ServerRequest rq) {
        standService.stop();

        return ServerResponse
                .ok()
                .header("Content-Language", "ru")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(standStatusData);
    }

    public Mono<ServerResponse> startService(ServerRequest rq) {
        standService.start();

        return ServerResponse
                .ok()
                .header("Content-Language", "ru")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(standStatusData);
    }

    public Mono<ServerResponse> restartService(ServerRequest rq) {
        standService.restart();

        return ServerResponse
                .ok()
                .header("Content-Language", "ru")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(standStatusData);
    }
}
