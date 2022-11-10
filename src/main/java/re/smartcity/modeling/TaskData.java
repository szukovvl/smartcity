package re.smartcity.modeling;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import re.smartcity.config.sockets.GameSocketHandler;
import re.smartcity.config.sockets.model.GameBlock;
import re.smartcity.config.sockets.model.PurchasedLot;
import re.smartcity.config.sockets.process.GameProcess;
import re.smartcity.energynet.component.MainSubstationPowerSystem;
import re.smartcity.modeling.data.GamerScenesData;
import re.smartcity.modeling.scheme.OesRootHub;
import re.smartcity.stand.StandService;
import re.smartcity.wind.WindRouterHandlers;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class TaskData {

    private final Object _lock = new Object();

    private final MainSubstationPowerSystem powerSystem;
    private final GamerScenesData scenesData;
    private ExecutorService service;
    private int[] choicesScene = new int[0]; // !!! пока а костылях
    private PurchasedLot[] auctionScene = new PurchasedLot[0];
    private volatile OesRootHub root;
    private volatile GameBlock gameBlock;

    public TaskData (
            MainSubstationPowerSystem powerSystem,
            GamerScenesData scenesData
    ) {
        this.powerSystem = powerSystem;
        this.scenesData = scenesData;
        this.root = OesRootHub.create(powerSystem);
    }

    public MainSubstationPowerSystem getPowerSystem() {
        return powerSystem;
    }

    public GamerScenesData getScenesData() {
        return scenesData;
    }

    public int[] getChoicesScene() {
        return choicesScene;
    }

    public PurchasedLot[] getAuctionScene() {
        return auctionScene;
    }

    public OesRootHub getRoot() {
        return root;
    }

    public void setRoot(OesRootHub root) {
        this.root = root;
    }

    public void setAuctionScene(PurchasedLot[] auctionScene) {
        this.auctionScene = auctionScene;
    }

    public void setChoicesScene(int[] choicesScene) {
        this.choicesScene = choicesScene;
    }

    public GameBlock getGameBlock() {
        return gameBlock;
    }

    public void setGameBlock(GameBlock gameBlock) {
        this.gameBlock = gameBlock;
    }

    public void startGame(
            GameSocketHandler messenger,
            ModelingData modelingData,
            StandService standService,
            WindRouterHandlers wind
    ) {
        Executors.newSingleThreadExecutor().execute(() -> {
            synchronized (_lock) {
                if (this.service != null) {
                    this.service.shutdownNow();
                    try {
                        this.service.awaitTermination(5, TimeUnit.SECONDS);
                    } catch (InterruptedException ignored) { }
                    this.service = null;
                }
                this.service = Executors.newSingleThreadExecutor();
                this.service.execute(new GameProcess(this, messenger, modelingData, standService, wind));
            }
        });
    }

    public void stopGame() {
        synchronized (_lock) {
            if (this.service != null) {
                this.service.shutdownNow();
            }
            this.service = null;
        }
    }
}
