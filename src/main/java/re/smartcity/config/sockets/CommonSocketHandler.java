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
import re.smartcity.config.sockets.model.CellDataEvent;
import re.smartcity.stand.StandStatusData;
import re.smartcity.sun.SunStatusData;
import re.smartcity.wind.WindStatusData;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

@Component
public class CommonSocketHandler implements WebSocketHandler {

    private final Logger logger = LoggerFactory.getLogger(CommonSocketHandler.class);

    private final Flux<String> outputEvents;
    private final ObjectMapper mapper;
    private final Sinks.Many<CommonServiceEvent<?>> eventPublisher;

    @Autowired
    private WindStatusData windStatus;

    @Autowired
    private SunStatusData sunStatus;

    @Autowired
    private StandStatusData standStatus;

    public CommonSocketHandler(Sinks.Many<CommonServiceEvent<?>> eventPublisher, Flux<CommonServiceEvent<?>> events) {
        this.mapper = new ObjectMapper();
        this.outputEvents = Flux.from(events).map(this::toJSON);
        this.eventPublisher = eventPublisher;

        /*Executors.newSingleThreadExecutor().execute(() -> {
            EventTypes[] types = EventTypes.values();
            Random random = new Random();
            try {
                while (true) {
                    Thread.sleep(3000);
                    eventPublisher.tryEmitNext(createEvent(types[random.nextInt(types.length)]));
                    eventPublisher.tryEmitNext(createEvent(types[random.nextInt(types.length)]));
                }
            }
            catch (InterruptedException ex) {
                logger.error(ex.getMessage());
            }
        });*/
    }

    /*
    private CustomEvent<?> createEvent(EventTypes event) {
        switch (event) {
            case STAND -> {
                return CustomEvent
                        .type(event)
                        .data(new StandEventData(true, null))
                        .build();
            }
            case SUN -> {
                return CustomEvent.type(event).data(new SunEventData(new Random().nextFloat(100.0f))).build();
            }
            case WIND -> {
                return CustomEvent.type(event).data(new WindEventData(new Random().nextFloat(100.0f), "local", null)).build();
            }
            case WIND_SLICE -> {
                return CustomEvent.type(event).data(new WindSliceEventData((byte) 0x10, new Random().nextFloat(100.0f))).build();
            }
            case SOLAR_SLICE -> {
                return CustomEvent.type(event).data(new SunSliceEventData((byte) 0x01, new Random().nextFloat(100.0f))).build();
            }
            default -> {
                logger.error("недопустимый тип события");
                throw new IllegalArgumentException("недопустимый тип события");
            }
        }
    }
    */

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
                /*.doFirst(() -> session.send(Flux.just(session.textMessage("Hi!")))
                        .subscribe())*/
                .doFirst(() -> {
                    session.send(Flux
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
                            .subscribe();
                })
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
