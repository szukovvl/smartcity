package re.smartcity.config.sockets.model;

public final class AuctionGamerData {
    private final int key;
    private PurchasedLot[] lots;

    public AuctionGamerData(int key, PurchasedLot[] lots) {
        this.key = key;
        this.lots = lots;
    }

    public AuctionGamerData(int key) {
        this(key, new PurchasedLot[0]);
    }

    public int getKey() {
        return key;
    }

    public PurchasedLot[] getLots() {
        return lots;
    }

    public void setLots(PurchasedLot[] lots) {
        this.lots = lots;
    }
}
