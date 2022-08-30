package re.smartcity.config.sockets;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import re.smartcity.common.resources.Messages;
import re.smartcity.config.sockets.model.*;
import re.smartcity.energynet.IComponentIdentification;
import re.smartcity.energynet.SupportedTypes;
import re.smartcity.energynet.component.Consumer;
import re.smartcity.modeling.GameStatuses;
import re.smartcity.modeling.ModelingData;
import re.smartcity.modeling.TaskData;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.SignalType;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class GameSocketHandler implements WebSocketHandler {

    private final Logger logger = LoggerFactory.getLogger(GameSocketHandler.class);

    private final Map<String, WebSocketSession> guests = new ConcurrentHashMap<>();
    private final ModelingData modelingData;
    private final ObjectMapper mapper = new ObjectMapper();

    private volatile WebSocketSession gameAdmin;
    private final WebSocketSession[] gamers = new WebSocketSession[2];

    private final Object _locked = new Object();

    public GameSocketHandler(ModelingData modelingData) {
        this.modelingData = modelingData;
    }

    private void onSubscribe(WebSocketSession session) {
        guests.put(session.getId(), session);
        sendEventToAll(buildStatusEvent());
    }

    private void onError(Throwable error) {
        logger.error(error.getMessage());
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
            return new GameClientEvent(GameEventTypes.ERROR, null);
        }
    }

    private <T> T fromJson(WebSocketSession session, String json, Class<T> clazz) {
        try {
            return mapper.readValue(json, clazz);
        } catch (IOException e) {
            logger.error(e.getMessage());
            sendEvent(session, GameServiceEvent
                    .type(GameEventTypes.ERROR)
                    .data(new GameErrorEvent(json, e.getMessage()))
                    .build());
            return null;
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
        switch (event.getType()) {
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
            case STARTGAMESCENES -> {
                if (gameAdmin != null && !session.getId().equals(gameAdmin.getId())) {
                    sendEvent(session, GameServiceEvent
                            .type(GameEventTypes.ERROR)
                            .data(new GameErrorEvent(event.getType().toString(), Messages.ER_14))
                            .build());
                }

                GameStartScenesEvent eventdata = fromJson(session, event.getPayload(), GameStartScenesEvent.class);
                if (eventdata != null) {
                    IComponentIdentification[] items = modelingData.getAllobjects();
                    for (StartScenesEventData datum : eventdata.getData()) {
                        TaskData task = Arrays.stream(modelingData.getTasks())
                                .filter(e -> e.getPowerSystem().getDevaddr() == datum.getMainstation())
                                .findFirst()
                                .orElse(null);
                        if (task == null) {
                            sendEvent(session, GameServiceEvent
                                    .type(GameEventTypes.ERROR)
                                    .data(new GameErrorEvent(event.getType().toString(),
                                            String.format(Messages.FER_6, datum.getMainstation())))
                                    .build());
                            return event;
                        }

                        List<IComponentIdentification> consumers = new ArrayList<>();
                        for (int consumer : datum.getConsumers()) {
                            IComponentIdentification item = Arrays.stream(items)
                                    .filter(e -> e.getDevaddr() == consumer && e.getComponentType() == SupportedTypes.CONSUMER)
                                    .findFirst()
                                    .orElse(null);
                            if (item == null) {
                                sendEvent(session, GameServiceEvent
                                        .type(GameEventTypes.ERROR)
                                        .data(new GameErrorEvent(event.getType().toString(),
                                                String.format(Messages.FER_6, consumer)))
                                        .build());
                                return event;
                            }
                            consumers.add(item);
                        }
                        task.getScenesData().setPredefconsumers(consumers.toArray(Consumer[]::new));
                    }

                    modelingData.setGamingDay(eventdata.getGameday());
                    modelingData.setGameStatus(GameStatuses.GAMERS_IDENTIFY);

                    sendEventToAll(buildStatusEvent());
                    sendEventToAll(GameServiceEvent
                            .type(GameEventTypes.GAME_SCENE_IDENTIFY)
                            .data(Arrays.stream(modelingData.getTasks())
                                    .map(e -> {
                                            Consumer[] defitems = e.getScenesData().getPredefconsumers();
                                            return ResponseScenesEventData
                                                    .builder(e.getPowerSystem().getDevaddr())
                                                    .substation(e.getScenesData().getSubstation().getDevaddr())
                                                    .consumers(defitems != null && defitems.length != 0
                                                            ? Arrays.stream(defitems)
                                                            .mapToInt(item -> (int) item.getDevaddr())
                                                            .toArray()
                                                            : null)
                                                    .build();
                                        })
                                    .toArray(ResponseScenesEventData[]::new))
                            .build());
                } else {
                    sendEvent(session, GameServiceEvent
                            .type(GameEventTypes.ERROR)
                            .data(new GameErrorEvent(event.getType().toString(), Messages.ER_13))
                            .build());
                }
            }
            case CANCELGAMESCENES -> {
                if (gameAdmin != null && !session.getId().equals(gameAdmin.getId())) {
                    sendEvent(session, GameServiceEvent
                            .type(GameEventTypes.ERROR)
                            .data(new GameErrorEvent(event.getType().toString(), Messages.ER_14))
                            .build());
                }

                modelingData.cancelScenes(); // !!!

                sendEventToAll(buildStatusEvent());
            }
            case SCENESDATA -> sendEvent(session, GameServiceEvent
                    .type(GameEventTypes.SCENESDATA)
                    .data(Arrays.stream(modelingData.getTasks())
                            .map(e -> ResponseScenesEventData
                                    .builder(e.getPowerSystem().getDevaddr())
                                    .substation(e.getScenesData().getSubstation().getDevaddr())
                                    .consumers(Arrays.stream(e.getScenesData().getPredefconsumers())
                                            .mapToInt(Consumer::getDevaddr)
                                            .toArray())
                                    .build())
                            .toArray(ResponseScenesEventData[]::new))
                    .build());
            case ERROR -> { }
            default -> sendEvent(session, GameServiceEvent
                    .type(GameEventTypes.ERROR)
                    .data(new GameErrorEvent(event.getType().toString(), "Неизвестный тип сообщения"))
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
                .doOnSubscribe(e -> onSubscribe(session))
                .doOnError(this::onError)
                .doFinally(e -> onFinally(session, e))
                .then();
    }
}
