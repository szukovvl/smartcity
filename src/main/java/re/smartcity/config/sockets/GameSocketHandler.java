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
import re.smartcity.modeling.data.GamerScenesData;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.SignalType;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.IntStream;

@Component
public class GameSocketHandler implements WebSocketHandler {

    private final Logger logger = LoggerFactory.getLogger(GameSocketHandler.class);

    private final Map<String, WebSocketSession> guests = new ConcurrentHashMap<>();
    private final ModelingData modelingData;
    private final ObjectMapper mapper = new ObjectMapper();

    private volatile WebSocketSession gameAdmin;
    private volatile GamerSession[] gamers = new GamerSession[0];
    private volatile int[] choicesScene; // !!! пока на костылях

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
            if (gameAdmin != null && session.getId().equals(gameAdmin.getId())) {
                this.gameAdmin = null;
            } else {
                synchronized (_locked) {
                    for (GamerSession gamer : this.gamers) {
                        if (gamer.getSession() != null && session.getId().equals(gamer.getSession().getId())) {
                            gamer.setSession(null);
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
                    if (guests.remove(session.getId()) == null) {
                        synchronized (_locked) {
                            for (GamerSession gamer : this.gamers) {
                                if (gamer.getSession() != null && session.getId().equals(gamer.getSession().getId())) {
                                    gamer.setSession(null);
                                }
                            }
                        }
                    }
                    this.gameAdmin = session;
                    sendEvent(session, GameServiceEvent
                            .type(GameEventTypes.GAMECONTROL)
                            .data(new GameAdminLockEvent(true, session.getId()))
                            .build());
                    sendEventToAll(buildStatusEvent());
                } else {
                    sendEvent(session, GameServiceEvent
                            .type(GameEventTypes.GAMECONTROL)
                            .data(new GameAdminLockEvent())
                            .build());
                }
            }
            case GAMER_ENTER -> {
                if (gameAdmin != null && session.getId().equals(gameAdmin.getId())) {
                    sendEvent(session, GameServiceEvent
                            .type(GameEventTypes.GAMER_ENTER)
                            .data(new GamerEnterEvent(modelingData.getGameStatus()))
                            .build());
                } else {
                    synchronized (_locked) {
                        if (gamers.length == 0) {
                            gamers = Arrays.stream(modelingData.getTasks())
                                    .map(e -> new GamerSession(e.getPowerSystem().getDevaddr()))
                                    .toArray(GamerSession[]::new);
                        }

                        GamerSession usedsession = Arrays.stream(gamers)
                                .filter(e -> e.getSession() != null && session.getId().equals(e.getSession().getId()))
                                .findFirst()
                                .orElse(null);
                        if (usedsession != null) { // уже является игроком
                            int finalKey = usedsession.getKey();
                            sendEvent(session, GameServiceEvent
                                    .type(GameEventTypes.GAMER_ENTER)
                                    .data(new GamerEnterEvent(
                                            true,
                                            usedsession.getSession().getId(),
                                            modelingData.getGameStatus(),
                                            usedsession.getKey(),
                                            Arrays.stream(buildScenesData())
                                                    .filter(e -> e.getMainstation() == finalKey)
                                                    .findFirst()
                                                    .orElse(null)
                                    ))
                                    .build());
                        } else {
                            byte key = 0;
                            String payload = event.getPayload();
                            if (payload != null && !payload.isEmpty()) {
                                try {
                                    key = Byte.parseByte(payload);
                                } catch (NumberFormatException ignored) {
                                }
                            }

                            if (key != 0) { // если есть предпочтения
                                byte finalKey = key;
                                usedsession = Arrays.stream(gamers)
                                        .filter(e -> e.getSession() == null && e.getKey() == finalKey)
                                        .findFirst()
                                        .orElse(null);
                            }
                            if (usedsession == null) { // ищем свободный ресурс
                                usedsession = Arrays.stream(gamers)
                                        .filter(e -> e.getSession() == null)
                                        .findFirst()
                                        .orElse(null);
                            }
                            if (usedsession != null) { // ресурс выделен
                                usedsession.setSession(session);
                                int finalKey = usedsession.getKey();
                                sendEvent(session, GameServiceEvent
                                        .type(GameEventTypes.GAMER_ENTER)
                                        .data(new GamerEnterEvent(
                                                true,
                                                usedsession.getSession().getId(),
                                                modelingData.getGameStatus(),
                                                usedsession.getKey(),
                                                Arrays.stream(buildScenesData())
                                                        .filter(e -> e.getMainstation() == finalKey)
                                                        .findFirst()
                                                        .orElse(null)
                                        ))
                                        .build());
                                guests.remove(session.getId());
                                sendEventToAll(buildStatusEvent());
                            } else {
                                sendEvent(session, GameServiceEvent
                                        .type(GameEventTypes.GAMER_ENTER)
                                        .data(new GamerEnterEvent(modelingData.getGameStatus()))
                                        .build());
                            }
                        }
                    }
                }
            }
            case STARTGAMESCENES -> {
                if (gameAdmin != null && !session.getId().equals(gameAdmin.getId())) {
                    sendEvent(session, GameServiceEvent
                            .type(GameEventTypes.ERROR)
                            .data(new GameErrorEvent(event.getType().toString(), Messages.ER_14))
                            .build());
                }

                // !!! для костылей
                synchronized (_locked) {
                    this.choicesScene = new int[0];
                    for (TaskData task : modelingData.getTasks()) {
                        task.setChoicesScene(new int[0]);
                    }
                }
                //

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
                    .data(buildScenesData())
                    .build());
            case SCENE_COMPLETTE_IDENTIFY -> {
                int gamerKey;
                synchronized (_locked) {
                    gamerKey = Arrays.stream(gamers)
                            .filter(e -> e.getSession() != null && session.getId().equals(e.getSession().getId()))
                            .map(GamerSession::getKey)
                            .findFirst()
                            .orElse(0);
                }
                GamerScenesData sceneData = null;
                if (gamerKey != 0) {
                    sceneData = Arrays.stream(modelingData.getTasks())
                            .filter(e -> e.getPowerSystem().getDevaddr() == gamerKey)
                            .map(TaskData::getScenesData)
                            .findFirst()
                            .orElse(null);
                }
                if (sceneData != null) {
                    sceneData.setSceneIdentify(fromJson(session, event.getPayload(), SceneIdentifyData.class));
                    sendEventToAll(GameServiceEvent
                            .type(GameEventTypes.SCENESDATA)
                            .data(buildScenesData())
                            .build());
                } else {
                    sendEvent(session, GameServiceEvent
                            .type(GameEventTypes.ERROR)
                            .data(new GameErrorEvent(event.getType().toString(), Messages.ER_7))
                            .build());
                }
            }
            case GAME_SCENE_NEXT -> {
                if (gameAdmin != null && !session.getId().equals(gameAdmin.getId())) {
                    sendEvent(session, GameServiceEvent
                            .type(GameEventTypes.ERROR)
                            .data(new GameErrorEvent(event.getType().toString(), Messages.ER_14))
                            .build());
                }

                switch (modelingData.getGameStatus()) {
                    case GAMERS_IDENTIFY -> modelingData.setGameStatus(GameStatuses.GAMERS_CHOICE_OES);
                    default -> {
                        sendEvent(session, GameServiceEvent
                                .type(GameEventTypes.ERROR)
                                .data(new GameErrorEvent(event.getType().toString(), Messages.ER_15))
                                .build());
                        return event;
                    }
                }

                sendEventToAll(buildStatusEvent());

                // !!! поковыляли на костылях
                switch (modelingData.getGameStatus()) {
                    // GAMERS_IDENTIFY - первая сцена
                    case GAMERS_CHOICE_OES -> sendEventToAll(GameServiceEvent
                            .type(GameEventTypes.GAME_SCENE_CHOICE_OES)
                            .data(buildChoiceSceneResponse())
                            .build());
                    default ->  sendEvent(session, GameServiceEvent
                            .type(GameEventTypes.ERROR)
                            .data(new GameErrorEvent(event.getType().toString(), Messages.ER_15))
                            .build());
                }
            }
            case GAME_SCENE_CHOICE_OES -> sendEventToAll(GameServiceEvent
                    .type(GameEventTypes.GAME_SCENE_CHOICE_OES)
                    .data(buildChoiceSceneResponse())
                    .build());
            case GAMER_CAPTURE_OES -> {
                int gamerKey;
                synchronized (_locked) {
                    gamerKey = Arrays.stream(gamers)
                            .filter(e -> e.getSession() != null && session.getId().equals(e.getSession().getId()))
                            .map(GamerSession::getKey)
                            .findFirst()
                            .orElse(0);
                }
                TaskData gamerTask = null;
                if (gamerKey != 0) {
                    gamerTask = Arrays.stream(modelingData.getTasks())
                            .filter(e -> e.getPowerSystem().getDevaddr() == gamerKey)
                            .findFirst()
                            .orElse(null);
                }
                if (gamerTask == null) {
                    sendEvent(session, GameServiceEvent
                            .type(GameEventTypes.ERROR)
                            .data(new GameErrorEvent(event.getType().toString(), Messages.ER_7))
                            .build());
                    return event;
                }
                int oesKey;
                try {
                    oesKey = Integer.parseInt(event.getPayload());
                }
                catch (NumberFormatException ex) {
                    sendEvent(session, GameServiceEvent
                            .type(GameEventTypes.ERROR)
                            .data(new GameErrorEvent(event.getType().toString(),
                                    String.format(Messages.FER_7, event.getPayload())))
                            .build());
                    return event;
                }

                if (oesKey == 0) {
                    sendEvent(session, GameServiceEvent
                            .type(GameEventTypes.ERROR)
                            .data(new GameErrorEvent(event.getType().toString(), Messages.ER_16))
                            .build());
                    return event;
                }

                ResponseChoiceOesData response;
                synchronized (_locked) {
                    if (Arrays.stream(gamerTask.getChoicesScene())
                            .filter(e -> e == oesKey)
                            .findFirst()
                            .isEmpty()) { // возможно такой объект уже присоединен...
                        if (Arrays.stream(this.choicesScene)
                                .filter(e -> e == oesKey)
                                .findFirst()
                                .isPresent()) { // объект доступен
                            this.choicesScene = Arrays.stream(this.choicesScene)
                                    .filter(e -> e != oesKey)
                                    .toArray();
                            int[] newitems = Arrays.copyOf(
                                    gamerTask.getChoicesScene(),
                                    gamerTask.getChoicesScene().length + 1);
                            newitems[newitems.length - 1] = oesKey;
                            gamerTask.setChoicesScene(newitems);
                        }
                    }
                    response = buildChoiceSceneResponse();
                }
                sendEventToAll(GameServiceEvent
                        .type(GameEventTypes.GAME_SCENE_CHOICE_OES)
                        .data(response)
                        .build());
            }
            case GAMER_REFUSE_OES -> {
                int gamerKey;
                synchronized (_locked) {
                    gamerKey = Arrays.stream(gamers)
                            .filter(e -> e.getSession() != null && session.getId().equals(e.getSession().getId()))
                            .map(GamerSession::getKey)
                            .findFirst()
                            .orElse(0);
                }
                TaskData gamerTask = null;
                if (gamerKey != 0) {
                    gamerTask = Arrays.stream(modelingData.getTasks())
                            .filter(e -> e.getPowerSystem().getDevaddr() == gamerKey)
                            .findFirst()
                            .orElse(null);
                }
                if (gamerTask == null) {
                    sendEvent(session, GameServiceEvent
                            .type(GameEventTypes.ERROR)
                            .data(new GameErrorEvent(event.getType().toString(), Messages.ER_7))
                            .build());
                    return event;
                }
                int oesKey;
                try {
                    oesKey = Integer.parseInt(event.getPayload());
                }
                catch (NumberFormatException ex) {
                    sendEvent(session, GameServiceEvent
                            .type(GameEventTypes.ERROR)
                            .data(new GameErrorEvent(event.getType().toString(),
                                    String.format(Messages.FER_7, event.getPayload())))
                            .build());
                    return event;
                }

                if (oesKey == 0) {
                    sendEvent(session, GameServiceEvent
                            .type(GameEventTypes.ERROR)
                            .data(new GameErrorEvent(event.getType().toString(), Messages.ER_16))
                            .build());
                    return event;
                }

                ResponseChoiceOesData response;
                synchronized (_locked) {
                    if (Arrays.stream(gamerTask.getChoicesScene())
                            .filter(e -> e == oesKey)
                            .findFirst()
                            .isPresent()) { // объект выбран игроком
                        gamerTask.setChoicesScene(Arrays.stream(gamerTask.getChoicesScene())
                                .filter(e -> e != oesKey)
                                .toArray());
                    }
                    response = buildChoiceSceneResponse();
                }
                sendEventToAll(GameServiceEvent
                        .type(GameEventTypes.GAME_SCENE_CHOICE_OES)
                        .data(response)
                        .build());
            }
            case ERROR -> { }
            default -> sendEvent(session, GameServiceEvent
                    .type(GameEventTypes.ERROR)
                    .data(new GameErrorEvent(event.getType().toString(), "Неизвестный тип сообщения"))
                    .build());
        }
        return event;
    }

    private synchronized ResponseChoiceOesData buildChoiceSceneResponse() {
        int[] seletedItems = new int[0];
        for (TaskData task : modelingData.getTasks()) {
            seletedItems = IntStream.concat(
                            IntStream.of(seletedItems),
                            IntStream.of(task.getChoicesScene()))
                    .toArray();
            seletedItems = IntStream.concat(
                            IntStream.of(seletedItems),
                            Arrays.stream(task.getScenesData().getPredefconsumers())
                                    .mapToInt(Consumer::getDevaddr))
                    .toArray();
        }

        int[] finalSeletedItems = seletedItems;
        // доступные объекты
        this.choicesScene = Arrays.stream(modelingData.getAllobjects())
                .filter(e -> e.getComponentType() == SupportedTypes.CONSUMER)
                .mapToInt(IComponentIdentification::getDevaddr)
                .filter(e -> Arrays.stream(finalSeletedItems)
                        .filter(v -> v == e)
                        .findFirst()
                        .isEmpty())
                .toArray();

        return new ResponseChoiceOesData(
                this.choicesScene,
                Arrays.stream(modelingData.getTasks())
                        .map(e -> new ChoiceOesData(
                                e.getPowerSystem().getDevaddr(),
                                e.getChoicesScene()))
                        .toArray(ChoiceOesData[]::new));
    }

    private ResponseScenesEventData[] buildScenesData() {
        return Arrays.stream(modelingData.getTasks())
                .map(e -> ResponseScenesEventData
                        .builder(e.getPowerSystem().getDevaddr())
                        .substation(e.getScenesData().getSubstation().getDevaddr())
                        .consumers(Arrays.stream(e.getScenesData().getPredefconsumers())
                                .mapToInt(Consumer::getDevaddr)
                                .toArray())
                        .sceneIdentify(e.getScenesData().getSceneIdentify())
                        .build())
                .toArray(ResponseScenesEventData[]::new);
    }

    private GameServiceEvent<?> buildStatusEvent() {
        synchronized (_locked) {
            return GameServiceEvent
                    .type(GameEventTypes.STATUS)
                    .data(new GameStatusEvent(
                            modelingData.getGameStatus(),
                            this.gameAdmin != null,
                            Arrays.stream(this.gamers)
                                    .filter(e -> e.getSession() != null)
                                    .toArray()
                                    .length,
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
                    .filter(e -> e.getSession() != null && e.getSession().isOpen())
                    .forEach(e -> sendEvent(e.getSession(), event));
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