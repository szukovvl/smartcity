package re.smartcity.modeling;

import re.smartcity.config.sockets.model.PurchasedLot;
import re.smartcity.energynet.component.MainSubstationPowerSystem;
import re.smartcity.modeling.data.GamerScenesData;
import re.smartcity.modeling.scheme.OesRootHub;

import java.util.concurrent.ExecutorService;

public class TaskData {

    private final ExecutorService service;
    private final MainSubstationPowerSystem powerSystem;
    private final GamerScenesData scenesData;
    private int[] choicesScene = new int[0]; // !!! пока а костылях
    private PurchasedLot[] auctionScene = new PurchasedLot[0];
    private OesRootHub root;

    public TaskData (
            ExecutorService service,
            MainSubstationPowerSystem powerSystem,
            GamerScenesData scenesData
    ) {
        this.powerSystem = powerSystem;
        this.service = service;
        this.scenesData = scenesData;
        this.root = OesRootHub.create(powerSystem);
    }

    public ExecutorService getService() {
        return service;
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
}
