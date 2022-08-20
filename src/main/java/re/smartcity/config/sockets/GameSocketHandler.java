package re.smartcity.config.sockets;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.reactivestreams.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import re.smartcity.config.sockets.model.GameAdminLockEvent;
import re.smartcity.config.sockets.model.GameErrorEvent;
import re.smartcity.config.sockets.model.GameStatusEvent;
import re.smartcity.modeling.ModelingData;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.SignalType;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class GameSocketHandler implements WebSocketHandler {

    private final Logger logger = LoggerFactory.getLogger(GameSocketHandler.class);

    private final Map<String, WebSocketSession> guests = new ConcurrentHashMap<>();
    private final ModelingData modelingData;
    private final ObjectMapper mapper = new ObjectMapper();

    private volatile WebSocketSession gameAdmin;
    private final WebSocketSession[] gamers = new WebSocketSession[0];

    private final Object _locked = new Object();

    public GameSocketHandler(ModelingData modelingData) {
        this.modelingData = modelingData;
    }

    private void onSubscribe(WebSocketSession session, Subscription data) {
        logger.info("-> onSubscribe: {}", guests.size());
        guests.put(session.getId(), session);
        sendEventToAll(buildStatusEvent());
    }

    private void onError(Throwable error) {
        logger.error(error.getMessage());
    }

    private void onComplete() {
        logger.info("-> (!) onComplete");
    }

    private void onFirst(WebSocketSession session) {
        //sendEvent(session, buildStatusEvent());
    }

    private void onCancel() {
        logger.warn("-> (!) onCancel");
    }

    private void onFinally(WebSocketSession session, SignalType sign) {
        if (guests.remove(session.getId()) == null) {
            if (session.equals(gameAdmin)) {
                this.gameAdmin = null;
            } else {
                synchronized (_locked) {
                    for (int i = 0; i < this.gamers.length; i++) {
                        if (gamers[i] != null && session.getId().equals(gamers[i].getId())) {
                            gamers[i] = null;
                        }
                    }
                }
            }
        }
        logger.info("-> onFinally: {}", guests.size());
        sendEventToAll(buildStatusEvent());
    }

    private GameClientEvent toClientEvent(WebSocketSession session, String json) {
        try {
            return mapper.readValue(json, GameClientEvent.class);
        } catch (IOException e) {
            logger.error(e.getMessage());
            sendEvent(session, GameServiceEvent
                    .type(GameEventTypes.ERROR)
                    .data(new GameErrorEvent(json, e.getMessage()))
                    .build());
            return new GameClientEvent(GameEventTypes.ERROR);
        }
    }

    private String toJSON(GameServiceEvent<?> event) {
        try {
            return mapper.writeValueAsString(event);
        } catch (JsonProcessingException e) {
            logger.error(e.getMessage());
            throw new RuntimeException(e);
        }
    }

    private GameClientEvent translateEvent(WebSocketSession session, GameClientEvent event) {
        switch (event.type()) {
            case STATUS -> sendEvent(session, buildStatusEvent());
            case GAMECONTROL -> {
                if (this.gameAdmin == null || session.getId().equals(this.gameAdmin.getId()))
                {
                    this.gameAdmin = session;
                    sendEvent(session, GameServiceEvent
                            .type(GameEventTypes.GAMECONTROL)
                            .data(new GameAdminLockEvent(true, session.getId()))
                            .build());
                    if (guests.remove(session.getId()) == null) {
                    } else {
                        synchronized (_locked) {
                            for (int i = 0; i < this.gamers.length; i++) {
                                if (gamers[i] != null && session.getId().equals(gamers[i].getId())) {
                                    gamers[i] = null;
                                }
                            }
                        }
                    }
                    sendEventToAll(buildStatusEvent());
                } else {
                    sendEvent(session, GameServiceEvent
                            .type(GameEventTypes.GAMECONTROL)
                            .data(new GameAdminLockEvent())
                            .build());
                }
            }
            case ERROR -> { }
            default -> sendEvent(session, GameServiceEvent
                    .type(GameEventTypes.ERROR)
                    .data(new GameErrorEvent(event.type().toString(), "Неизвестный тип сообщения"))
                    .build());
        }
        return event;
    }

    private GameServiceEvent<?> buildStatusEvent() {
        synchronized (_locked) {
            return GameServiceEvent
                    .type(GameEventTypes.STATUS)
                    .data(new GameStatusEvent(
                            modelingData.getGameStatus(),
                            this.gameAdmin != null,
                            Arrays.stream(this.gamers).filter(Objects::nonNull).toArray().length,
                            this.guests.size()))
                    .build();
        }
    }

    private void sendEvent(WebSocketSession session, GameServiceEvent<?> event) {
        session.send(Flux
                        .just(session.textMessage(toJSON(event))))
                .subscribe();
    }

    private void sendEventToAll(GameServiceEvent<?> event) {
        if (this.gameAdmin != null && this.gameAdmin.isOpen()) {
            sendEvent(this.gameAdmin, event);
        }
        synchronized (_locked) {
            Arrays.stream(this.gamers)
                    .filter(e -> e != null && e.isOpen())
                    .forEach(e -> sendEvent(e, event));
        }
        this.guests.values()
                .stream()
                .filter(WebSocketSession::isOpen)
                .forEach(e -> sendEvent(e, event));
    }

    @Override
    public Mono<Void> handle(WebSocketSession session) {
        return session
                .receive()
                .map(WebSocketMessage::getPayloadAsText)
                .map(e -> toClientEvent(session, e))
                .map(e -> translateEvent(session, e))
                .doOnSubscribe(e -> onSubscribe(session, e))
                .doOnError(this::onError)
                .doOnComplete(this::onComplete)
                .doFirst(() -> onFirst(session))
                .doOnCancel(this::onCancel)
                .doFinally(e -> onFinally(session, e))
                .then();
    }
}
