package re.smartcity.stand;

import jssc.SerialPortList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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

    public Mono<ServerResponse> getControl(ServerRequest ignoredRq) {
        return ServerResponse
                .ok()
                .header("Content-Language", "ru")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(standService.getControlData());
    }

    public Mono<ServerResponse> putControl(ServerRequest rq) {
        return rq.bodyToMono(StandControlData.class)
                .flatMap(data -> standService.setControlData(data))
                .flatMap(rows -> {
                    if (rows != 0) {
                        return ServerResponse
                                .ok()
                                .header("Content-Language", "ru")
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue(standService.getControlData());
                    } else {
                        return ServerResponse
                                .status(HttpStatus.NOT_IMPLEMENTED)
                                .header("Content-Language", "ru")
                                .contentType(MediaType.TEXT_PLAIN)
                                .bodyValue("данные не были синхронизированы");
                    }
                })
                .onErrorResume(t -> ServerResponse
                        .status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .contentType(MediaType.TEXT_PLAIN)
                        .bodyValue(t.getMessage()));
    }

    public Mono<ServerResponse> getPortNames(ServerRequest ignoredRq) {
        return ServerResponse
                .ok()
                .header("Content-Language", "ru")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(SerialPortList.getPortNames());
    }

    // управление сервисом
    public Mono<ServerResponse> stopService(ServerRequest ignoredRq) {
        standService.stop();

        return ServerResponse
                .ok()
                .header("Content-Language", "ru")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(standStatusData);
    }

    public Mono<ServerResponse> startService(ServerRequest ignoredRq) {
        standService.start();

        return ServerResponse
                .ok()
                .header("Content-Language", "ru")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(standStatusData);
    }

    public Mono<ServerResponse> restartService(ServerRequest ignoredRq) {
        standService.restart();

        return ServerResponse
                .ok()
                .header("Content-Language", "ru")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(standStatusData);
    }
}
