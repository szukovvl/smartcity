package re.smartcity.config.sockets;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import re.smartcity.stand.StandStatusData;
import re.smartcity.sun.SunStatusData;
import re.smartcity.wind.WindStatusData;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

@Component
public class CommonSocketHandler implements WebSocketHandler {

    private final Logger logger = LoggerFactory.getLogger(CommonSocketHandler.class);

    private final ObjectMapper mapper = new ObjectMapper();
    private final Sinks.Many<CommonServiceEvent<?>> eventPublisher = Sinks
            .many()
            .replay()
            .latest();
    private final Flux<CommonServiceEvent<?>> events = eventPublisher
            .asFlux()
            .replay(1)
            .autoConnect();
    private final Flux<String> outputEvents = Flux.from(events).map(this::toJSON);

    @Autowired
    private WindStatusData windStatus;

    @Autowired
    private SunStatusData sunStatus;

    @Autowired
    private StandStatusData standStatus;

    public CommonSocketHandler() { }

    public synchronized <T> void pushEvent(CommonEventTypes type, T data) {
        eventPublisher.tryEmitNext(
                CommonServiceEvent
                        .type(type)
                        .data(data)
                        .build());
    }

    @Override
    public Mono<Void> handle(WebSocketSession session) {

        return session
                .receive()
                .map(WebSocketMessage::getPayloadAsText)
                .doOnError(this::onError)
                .doOnComplete(this::onComplete)
                .doFirst(() -> session.send(Flux
                        .just(session.textMessage(
                                toJSON(CommonServiceEvent
                                        .type(CommonEventTypes.STAND)
                                        .data(standStatus)
                                        .build())
                        ))
                        .concatWithValues(
                                session.textMessage(
                                        toJSON(CommonServiceEvent
                                                .type(CommonEventTypes.SUN)
                                                .data(sunStatus)
                                                .build())
                                ),
                                session.textMessage(
                                        toJSON(CommonServiceEvent
                                                .type(CommonEventTypes.WIND)
                                                .data(windStatus)
                                                .build())
                                )))
                        .subscribe())
                .zipWith(session.send(outputEvents.map(session::textMessage)))
                .then();
    }

    private void onError(Throwable error) {
        logger.error(error.getMessage());
    }

    private void onComplete() { }

    private String toJSON(CommonServiceEvent<?> event) {
        try {
            return mapper.writeValueAsString(event);
        } catch (JsonProcessingException e) {
            logger.error(e.getMessage());
            throw new RuntimeException(e);
        }
    }
}
