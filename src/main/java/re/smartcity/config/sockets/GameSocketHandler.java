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
import re.smartcity.common.CommonStorage;
import re.smartcity.common.data.Tariffs;
import re.smartcity.common.resources.Messages;
import re.smartcity.config.sockets.model.*;
import re.smartcity.energynet.IComponentIdentification;
import re.smartcity.energynet.SupportedTypes;
import re.smartcity.energynet.component.Consumer;
import re.smartcity.energynet.component.MainSubstationPowerSystem;
import re.smartcity.modeling.GameStatuses;
import re.smartcity.modeling.ModelingData;
import re.smartcity.modeling.TaskData;
import re.smartcity.modeling.data.AuctionSettings;
import re.smartcity.modeling.data.GamerScenesData;
import re.smartcity.modeling.scheme.IConnectionPort;
import re.smartcity.modeling.scheme.IOesHub;
import re.smartcity.modeling.scheme.OesRootHub;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.SignalType;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@Component
public class GameSocketHandler implements WebSocketHandler {

    private final Logger logger = LoggerFactory.getLogger(GameSocketHandler.class);

    @Autowired
    private CommonStorage commonStorage;

    private final Map<String, WebSocketSession> guests = new ConcurrentHashMap<>();
    private final ModelingData modelingData;
    private final ObjectMapper mapper = new ObjectMapper();

    private volatile WebSocketSession gameAdmin;
    private volatile GamerSession[] gamers = new GamerSession[0];
    private volatile int[] choicesScene; // !!! пока на костылях
    private volatile int[] auctionLots;
    private volatile int[] unsoldLots;
    private volatile PurchasedLot currentLot;
    private volatile int buyer;
    private volatile double buyPrice;
    private volatile AuctionSettings auctionSettings = new AuctionSettings();
    private ExecutorService auctionProcessing = null;

    private final Object _locked = new Object();

    public GameSocketHandler(ModelingData modelingData) {
        this.modelingData = modelingData;
        modelingData.setMessenger(this);
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
                    return event;
                }

                // !!! для костылей
                synchronized (_locked) {
                    this.choicesScene = new int[0];
                    this.currentLot = null;
                    this.auctionLots = new int[0];
                    this.unsoldLots = new int[0];
                    for (TaskData task : modelingData.getTasks()) {
                        task.setChoicesScene(new int[0]);
                        task.setAuctionScene(new PurchasedLot[0]);
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
                            .data(buildIdentySceneResponse())
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
                    return event;
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
                    return event;
                }

                if (this.auctionProcessing != null) {
                    sendEvent(session, GameServiceEvent
                            .type(GameEventTypes.ERROR)
                            .data(new GameErrorEvent(event.getType().toString(), Messages.ER_17))
                            .build());
                    return event;
                }

                switch (modelingData.getGameStatus()) {
                    case GAMERS_IDENTIFY -> modelingData.setGameStatus(GameStatuses.GAMERS_CHOICE_OES);
                    case GAMERS_CHOICE_OES -> modelingData.setGameStatus(GameStatuses.GAMERS_AUCTION_PREPARE);
                    case GAMERS_AUCTION_PREPARE -> modelingData.setGameStatus(GameStatuses.GAMERS_AUCTION_SALE);
                    case GAMERS_AUCTION_SALE -> modelingData.setGameStatus(GameStatuses.GAMERS_SCHEME);
                    case GAMERS_SCHEME -> modelingData.setGameStatus(GameStatuses.GAME_PROCESS);
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
                    case GAMERS_AUCTION_PREPARE -> sendEventToAll(GameServiceEvent
                            .type(GameEventTypes.GAME_SCENE_AUCTION_PREPARE)
                            .data(buildAuctionSceneResponse())
                            .build());
                    case GAMERS_AUCTION_SALE -> sendEventToAll(GameServiceEvent
                            .type(GameEventTypes.GAME_SCENE_AUCTION_SALE)
                            .data(buildAuctionSceneResponse())
                            .build());
                    case GAMERS_SCHEME -> {
                        sendEventToAll(GameServiceEvent
                                .type(GameEventTypes.GAME_SCENE_SCHEME)
                                .data(buildSchemeResponse())
                                .build());
                        sendSchemeDataMessage(null);
                    }
                    case GAME_PROCESS -> {
                        sendEventToAll(GameServiceEvent
                                .type(GameEventTypes.GAME_PROCESS_START)
                                .data(prepareGame())
                                .build());
                        sendEventToAll(GameServiceEvent
                                .type(GameEventTypes.GAME_PROCESS_ITERATION)
                                .data(0)
                                .build());
                    }
                    default ->  sendEvent(session, GameServiceEvent
                            .type(GameEventTypes.ERROR)
                            .data(new GameErrorEvent(event.getType().toString(), Messages.ER_15))
                            .build());
                }
            }
            case GAME_SCENE_PREV -> {
                if (gameAdmin != null && !session.getId().equals(gameAdmin.getId())) {
                    sendEvent(session, GameServiceEvent
                            .type(GameEventTypes.ERROR)
                            .data(new GameErrorEvent(event.getType().toString(), Messages.ER_14))
                            .build());
                    return event;
                }

                if (this.auctionProcessing != null) {
                    sendEvent(session, GameServiceEvent
                            .type(GameEventTypes.ERROR)
                            .data(new GameErrorEvent(event.getType().toString(), Messages.ER_17))
                            .build());
                    return event;
                }

                switch (modelingData.getGameStatus()) {
                    case GAMERS_CHOICE_OES -> modelingData.setGameStatus(GameStatuses.GAMERS_IDENTIFY);
                    case GAMERS_AUCTION_PREPARE -> modelingData.setGameStatus(GameStatuses.GAMERS_CHOICE_OES);
                    case GAMERS_AUCTION_SALE -> modelingData.setGameStatus(GameStatuses.GAMERS_AUCTION_PREPARE);
                    case GAMERS_SCHEME -> modelingData.setGameStatus(GameStatuses.GAMERS_AUCTION_SALE);
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
                    case GAMERS_IDENTIFY -> sendEventToAll(GameServiceEvent
                            .type(GameEventTypes.GAME_SCENE_IDENTIFY)
                            .data(buildIdentySceneResponse())
                            .build());
                    case GAMERS_CHOICE_OES -> sendEventToAll(GameServiceEvent
                            .type(GameEventTypes.GAME_SCENE_CHOICE_OES)
                            .data(buildChoiceSceneResponse())
                            .build());
                    case GAMERS_AUCTION_PREPARE -> sendEventToAll(GameServiceEvent
                            .type(GameEventTypes.GAME_SCENE_AUCTION_PREPARE)
                            .data(buildAuctionSceneResponse())
                            .build());
                    case GAMERS_AUCTION_SALE -> sendEventToAll(GameServiceEvent
                            .type(GameEventTypes.GAME_SCENE_AUCTION_SALE)
                            .data(buildAuctionSceneResponse())
                            .build());
                    default ->  sendEvent(session, GameServiceEvent
                            .type(GameEventTypes.ERROR)
                            .data(new GameErrorEvent(event.getType().toString(), Messages.ER_15))
                            .build());
                }
            }
            case GAME_SCENE_CHOICE_OES -> sendEvent(session, GameServiceEvent
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
            case GAME_SCENE_AUCTION -> sendEvent(session, GameServiceEvent
                    .type(GameEventTypes.GAME_SCENE_AUCTION)
                    .data(buildAuctionSceneResponse())
                    .build());
            case GAME_SCENE_AUCTION_SETTINGS -> {
                if (gameAdmin != null && !session.getId().equals(gameAdmin.getId())) {
                    sendEvent(session, GameServiceEvent
                            .type(GameEventTypes.ERROR)
                            .data(new GameErrorEvent(event.getType().toString(), Messages.ER_14))
                            .build());
                    return event;
                }

                AuctionSettings params = fromJson(session, event.getPayload(), AuctionSettings.class);
                if (params != null) {
                    this.auctionSettings = params;
                    sendEventToAll(GameServiceEvent
                            .type(GameEventTypes.GAME_SCENE_AUCTION)
                            .data(buildAuctionSceneResponse())
                            .build());
                }
            }
            case GAME_SCENE_AUCTION_PUT_LOT -> {
                if (gameAdmin != null && !session.getId().equals(gameAdmin.getId())) {
                    sendEvent(session, GameServiceEvent
                            .type(GameEventTypes.ERROR)
                            .data(new GameErrorEvent(event.getType().toString(), Messages.ER_14))
                            .build());
                    return event;
                }

                synchronized (_locked) {
                    if (this.auctionProcessing != null) {
                        sendEvent(session, GameServiceEvent
                                .type(GameEventTypes.ERROR)
                                .data(new GameErrorEvent(event.getType().toString(), Messages.ER_17))
                                .build());
                        return event;
                    }

                    if (this.auctionLots.length == 0) {
                        sendEvent(session, GameServiceEvent
                                .type(GameEventTypes.ERROR)
                                .data(new GameErrorEvent(event.getType().toString(), Messages.ER_18))
                                .build());
                        return event;
                    }

                    this.buyer = 0;
                    this.buyPrice = 0.0;
                    this.currentLot = new PurchasedLot(
                            this.auctionLots[(new Random()).nextInt(this.auctionLots.length)],
                            this.auctionSettings.getStartingcost());
                    sendEventToAll(GameServiceEvent
                            .type(GameEventTypes.GAME_SCENE_AUCTION)
                            .data(buildAuctionSceneResponse())
                            .build());
                    this.auctionProcessing = Executors.newSingleThreadExecutor();
                    this.auctionProcessing.execute(new LotExecutor(this.auctionProcessing));
                }
            }
            case GAME_SCENE_AUCTION_CANCEL_LOT -> {
                if (gameAdmin != null && !session.getId().equals(gameAdmin.getId())) {
                    sendEvent(session, GameServiceEvent
                            .type(GameEventTypes.ERROR)
                            .data(new GameErrorEvent(event.getType().toString(), Messages.ER_14))
                            .build());
                    return event;
                }

                ExecutorService process;
                synchronized (_locked) {
                    this.currentLot = null;
                    process = this.auctionProcessing;
                }
                if (process != null) {
                    process.shutdownNow();
                    try {
                        process.awaitTermination(1, TimeUnit.MILLISECONDS);
                    }
                    catch (InterruptedException ignored) { }
                }
                sendEventToAll(GameServiceEvent
                        .type(GameEventTypes.GAME_SCENE_AUCTION)
                        .data(buildAuctionSceneResponse())
                        .build());
            }
            case GAME_SCENE_AUCTION_BAY_LOT -> {
                int gamerKey;
                synchronized (_locked) {
                    gamerKey = Arrays.stream(gamers)
                            .filter(e -> e.getSession() != null && session.getId().equals(e.getSession().getId()))
                            .map(GamerSession::getKey)
                            .findFirst()
                            .orElse(0);
                }

                synchronized (_locked) {
                    if (gamerKey != 0 && this.buyer == gamerKey) {
                        return event;
                    }
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

                ExecutorService process;
                synchronized (_locked) {
                    process = this.auctionProcessing;
                }
                if (process != null) {
                    process.shutdownNow();
                    try {
                        process.awaitTermination(1, TimeUnit.MILLISECONDS);
                    }
                    catch (InterruptedException ignored) { }
                    synchronized (_locked) {
                        if (this.currentLot != null) {
                            this.buyer = gamerKey;
                            this.buyPrice = this.currentLot.price();
                            this.currentLot = new PurchasedLot(
                                    this.currentLot.key(),
                                    this.currentLot.price() + this.auctionSettings.getStartingcost() * 0.1);
                            this.auctionProcessing = Executors.newSingleThreadExecutor();
                            process = this.auctionProcessing;
                        }
                    }
                    Executors.newSingleThreadExecutor().execute(() -> { // !!! очередной костыль
                        sendEventToAll(GameServiceEvent
                                .type(GameEventTypes.GAME_SCENE_AUCTION)
                                .data(buildAuctionSceneResponse())
                                .build());
                    });
                    synchronized (_locked) {
                        if (process == this.auctionProcessing) {
                            process.execute(new LotExecutor(process));
                        }
                    }
                } else {
                    sendEventToAll(GameServiceEvent
                            .type(GameEventTypes.GAME_SCENE_AUCTION)
                            .data(buildAuctionSceneResponse())
                            .build());
                }
            }
            case GAME_SCENE_SCHEME -> sendEvent(session, GameServiceEvent
                    .type(GameEventTypes.GAME_SCENE_SCHEME)
                    .data(buildSchemeResponse())
                    .build());
            case GAME_SCHEMA_DATA -> sendSchemeDataMessage(session);
            case ERROR -> { }
            default -> sendEvent(session, GameServiceEvent
                    .type(GameEventTypes.ERROR)
                    .data(new GameErrorEvent(event.getType().toString(), "Неизвестный тип сообщения"))
                    .build());
        }
        return event;
    }

    private synchronized ResponseScenesEventData[] buildIdentySceneResponse() {
        return Arrays.stream(modelingData.getTasks())
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
                .toArray(ResponseScenesEventData[]::new);
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

    private synchronized ResponseAuctionData buildAuctionSceneResponse() {
        int[] busyLots = new int[0];
        // собираем по игрокам
        for (TaskData task : modelingData.getTasks()) {
            busyLots = IntStream.concat(
                            IntStream.of(busyLots),
                            Arrays.stream(task.getAuctionScene()).mapToInt(PurchasedLot::key))
                    .toArray();
        }

        // включаю отказников
        busyLots = IntStream.concat(
                        IntStream.of(busyLots),
                        IntStream.of(this.unsoldLots))
                .toArray();

        // выбираю только доступные
        int[] finalBusyLots = busyLots;
        this.auctionLots = Arrays.stream(modelingData.getAllobjects())
                .filter(e -> e.getComponentType() == SupportedTypes.GENERATOR ||
                        e.getComponentType() == SupportedTypes.GREEGENERATOR ||
                        e.getComponentType() == SupportedTypes.STORAGE)
                .mapToInt(IComponentIdentification::getDevaddr)
                .filter(e -> Arrays.stream(finalBusyLots)
                        .filter(v -> v == e)
                        .findFirst()
                        .isEmpty())
                .toArray();

        // проверяю выставленный лот
        if (this.currentLot != null) {
            if (Arrays.stream(this.auctionLots)
                    .filter(e -> e == this.currentLot.key())
                    .findFirst()
                    .isEmpty()) {
                this.currentLot = null;
            }
        }

        // собираю ответ
        return new ResponseAuctionData(
                this.auctionSettings,
                Arrays.stream(modelingData.getTasks())
                        .map(e -> new AuctionGamerData(e.getPowerSystem().getDevaddr(), e.getAuctionScene()))
                        .toArray(AuctionGamerData[]::new),
                this.auctionLots,
                this.unsoldLots,
                this.buyer,
                this.currentLot,
                modelingData.getGameStatus()
        );
    }

    private synchronized ResponseSchemeData[] buildSchemeResponse() {
        List<ResponseSchemeData> data = new ArrayList<>();

        // !!! кривова-то как-то
        AtomicReference<Double> tp = new AtomicReference<>(0.0);
        commonStorage.getAndCreate(Tariffs.key, Tariffs.class)
                .subscribe(e -> {
                    synchronized (tp) {
                        tp.set(e.getData().getTech_price());
                        tp.notifyAll();
                    }
                });

        try {
            synchronized (tp) {
                tp.wait(5000);
            }
        }
        catch (InterruptedException ex) {
            logger.error(ex.getMessage());
        }

        for (TaskData task : modelingData.getTasks()) {
            ResponseSchemeDataBuilder builder = ResponseSchemeData.build(task.getPowerSystem().getDevaddr());

            builder
                    .substation(task.getScenesData().getSubstation().getDevaddr())
                    .consumers(Arrays.stream(task.getScenesData().getPredefconsumers())
                            .mapToInt(Consumer::getDevaddr)
                            .toArray())
                    .tcconsumers(task.getChoicesScene())
                    .tcprice(tp.get())
                    .generators(task.getAuctionScene());

            data.add(builder.build());
        }

        return data.toArray(ResponseSchemeData[]::new);
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

    private GameBlock[] prepareGame() {
        return Arrays.stream(modelingData.getTasks())
                .map(e -> buildGameBlock(e.getRoot()))
                .toArray(GameBlock[]::new);
    }

    private GameBlock buildGameBlock(OesRootHub root) {
        return GameBlock.builder()
                .root(prepareRootHub(root))
                .build();
    }

    private void buildUnconnectedGameDevices() {
        /*
        List<ResponseSchemeData> data = new ArrayList<>();

        // !!! кривова-то как-то
        AtomicReference<Double> tp = new AtomicReference<>(0.0);
        commonStorage.getAndCreate(Tariffs.key, Tariffs.class)
                .subscribe(e -> {
                    synchronized (tp) {
                        tp.set(e.getData().getTech_price());
                        tp.notifyAll();
                    }
                });

        try {
            synchronized (tp) {
                tp.wait(5000);
            }
        }
        catch (InterruptedException ex) {
            logger.error(ex.getMessage());
        }

        for (TaskData task : modelingData.getTasks()) {
            ResponseSchemeDataBuilder builder = ResponseSchemeData.build(task.getPowerSystem().getDevaddr());

            builder
                    .substation(task.getScenesData().getSubstation().getDevaddr())
                    .consumers(Arrays.stream(task.getScenesData().getPredefconsumers())
                            .mapToInt(Consumer::getDevaddr)
                            .toArray())
                    .tcconsumers(task.getChoicesScene())
                    .tcprice(tp.get())
                    .generators(task.getAuctionScene());

            data.add(builder.build());
        }

         */
    }

    private OesRootHub prepareRootHub(OesRootHub source) {
        OesRootHub dest = OesRootHub.create((MainSubstationPowerSystem) source.getOwner());

        //
        IOesHub[] filteredDevices = Arrays.stream(source.getDevices() != null
                        ? source.getDevices()
                        : new IOesHub[0])
                .filter(IOesHub::hasOwner)
                .filter(e -> !e.isAlien())
                .filter(e -> !e.hasError())
                .toArray(IOesHub[]::new);
        List<IOesHub> actualDevices = new ArrayList<>();

        Arrays.stream(source.getInputs())
                .filter(e -> !e.hasError())
                .filter(e -> e.getConnections() != null)
                .forEach(line -> {
                    IConnectionPort destPort = Arrays.stream(dest.getInputs())
                            .filter(e -> e.getAddress() == line.getAddress())
                            .findFirst()
                            .orElseThrow();
                    Arrays.stream(line.getConnections())
                            .forEach(pt -> prepareConnections(pt, destPort, filteredDevices, actualDevices));
                });
        Arrays.stream(source.getOutputs())
                .filter(e -> !e.hasError())
                .filter(e -> e.getConnections() != null)
                .forEach(line -> {
                    IConnectionPort destPort = Arrays.stream(dest.getOutputs())
                            .filter(e -> e.getAddress() == line.getAddress())
                            .findFirst()
                            .orElseThrow();
                    Arrays.stream(line.getConnections())
                            .forEach(pt -> prepareConnections(pt, destPort, filteredDevices, actualDevices));
                });

        // подключение портов
        Stream.concat(Stream.of(dest.getInputs()), Stream.of(dest.getOutputs()))
                        .forEach(item -> item.setOn(true));
        actualDevices.forEach(item -> {
            if (item.getOwner().getComponentType() == SupportedTypes.DISTRIBUTOR) {
                Stream.concat(Stream.of(item.getInputs()), Stream.of(item.getOutputs()))
                        .forEach(distributor -> distributor.setOn(true));
            } else {
                item.getInputs()[0].setOn(true);
            }
        });

        dest.setDevices(actualDevices.toArray(IOesHub[]::new));

        return dest;
    }

    private void prepareConnections(IConnectionPort srcPort, IConnectionPort dstPort,
                                    IOesHub[] allDev, List<IOesHub> actualDev) {
        IOesHub hub = actualDev.stream()
                .filter(e -> e.itIsMine(srcPort.getAddress()))
                .findFirst()
                .orElse(null);
        if (hub == null) {
            hub = Arrays.stream(allDev)
                    .filter(e -> e.itIsMine(srcPort.getAddress()))
                    .findFirst()
                    .orElse(null);
            if (hub != null) {
                IOesHub newhub = OesRootHub.createOther(hub.getOwner());
                actualDev.add(newhub);
                if (hub.getOwner().getComponentType() == SupportedTypes.DISTRIBUTOR) {
                    // обработка миниподстанции
                    Arrays.stream(hub.getOutputs())
                            .filter(e -> !e.hasError())
                            .filter(e -> e.getConnections() != null)
                            .forEach(line -> Arrays.stream(line.getConnections())
                                    .forEach(port -> prepareConnections(
                                            port,
                                            newhub.connectionByAddress(line.getAddress()),
                                            allDev,
                                            actualDev)));
                }
                hub = newhub;
            }
        }
        if (hub != null) {
            dstPort.addConnection(hub.connectionByAddress(srcPort.getAddress()));
        }
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

    public void sendSchemeDataMessage(WebSocketSession session) {
        Arrays.stream(modelingData.getTasks())
                .forEach(task -> {
                    int[] all = IntStream.concat(
                            IntStream.concat(
                                    IntStream.of(task.getScenesData().getSubstation().getDevaddr()),
                                    Arrays.stream(task.getAuctionScene())
                                            .mapToInt(PurchasedLot::key)
                                    ),
                            IntStream.concat(
                                    Arrays.stream(task.getScenesData().getPredefconsumers() != null
                                                    ? task.getScenesData().getPredefconsumers()
                                                    : new Consumer[0])
                                            .mapToInt(Consumer::getDevaddr),
                                    IntStream.of(task.getChoicesScene()))
                            )
                            .toArray();
                    int[] addresses = Arrays.stream(task.getRoot().getDevices() != null
                                    ? task.getRoot().getDevices()
                                    : new IOesHub[0])
                            .mapToInt(IOesHub::getAddress)
                            .toArray();
                    int[] missed = Arrays.stream(all)
                            .filter(addr -> Arrays.stream(addresses)
                                    .noneMatch(hubaddr -> hubaddr == addr))
                            .toArray();

                    task.getRoot().setMissed(missed.length != 0 ? missed : null);

                    Arrays.stream(task.getRoot().getDevices() != null
                                    ? task.getRoot().getDevices()
                                    : new IOesHub[0])
                            .forEach(hub -> hub.setAlien(Arrays.stream(all)
                                    .noneMatch(b -> hub.getAddress() == b)));
                });

        GameServiceEvent<?> event = GameServiceEvent
                .type(GameEventTypes.GAME_SCHEMA_DATA)
                .data(Arrays.stream(modelingData.getTasks())
                        .map(TaskData::getRoot)
                        .toArray(IOesHub[]::new))
                .build();
        if (session != null) {
            sendEvent(session, event);
        } else {
            sendEventToAll(GameServiceEvent
                    .type(GameEventTypes.GAME_SCHEMA_DATA)
                    .data(Arrays.stream(modelingData.getTasks())
                            .map(TaskData::getRoot)
                            .toArray(IOesHub[]::new))
                    .build());
        }
    }

    private class LotExecutor implements Runnable {

        private final ExecutorService executor;

        private LotExecutor(ExecutorService executor) {
            this.executor = executor;
        }

        @Override
        public void run() {
            try {
                try {
                    int ticks = auctionSettings.getLotwaiting();
                    sendEventToAll(GameServiceEvent
                            .type(GameEventTypes.GAME_SCENE_AUCTION_TIME_LOT)
                            .data(ticks)
                            .build());
                    while (!this.executor.isShutdown() && !this.executor.isTerminated() && ticks > 0) {
                        Thread.sleep(1000L); // ответ по секунде
                        ticks--;
                        sendEventToAll(GameServiceEvent
                                .type(GameEventTypes.GAME_SCENE_AUCTION_TIME_LOT)
                                .data(ticks)
                                .build());
                    }
                    if (this.executor.isShutdown() || this.executor.isTerminated()) {
                        return;
                    }
                }
                catch (InterruptedException ex) {
                    return;
                }
                synchronized (_locked) {
                    // действия с текущим лотом не выполнялись - лот отказан, а может куплен?
                    if (currentLot != null) {
                        if (buyer != 0) { // лот отошел игроку
                            TaskData data = Arrays.stream(modelingData.getTasks())
                                    .filter(e -> e.getPowerSystem().getDevaddr() == buyer)
                                    .findFirst()
                                    .orElse(null);
                            if (data != null) {
                                PurchasedLot[] gamerLots = data.getAuctionScene();
                                gamerLots = Arrays.copyOf(gamerLots, gamerLots.length + 1);
                                gamerLots[gamerLots.length - 1] = new PurchasedLot(currentLot.key(), buyPrice);
                                data.setAuctionScene(gamerLots);
                            }
                        } else { // лот отказан
                            unsoldLots = Arrays.copyOf(
                                    unsoldLots,
                                    unsoldLots.length + 1);
                            unsoldLots[unsoldLots.length - 1] = currentLot.key();
                        }
                        currentLot = null;
                        buyer = 0;
                    }
                }
            }
            finally {
                synchronized (_locked) {
                    if (this.executor == auctionProcessing) {
                        auctionProcessing = null;
                    }
                }
                sendEventToAll(GameServiceEvent
                        .type(GameEventTypes.GAME_SCENE_AUCTION)
                        .data(buildAuctionSceneResponse())
                        .build());
            }
        }
    }
}
