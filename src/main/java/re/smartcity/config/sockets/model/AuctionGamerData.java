package re.smartcity.config.sockets.model;

public final class AuctionGamerData {
    private final int key;
    private int[] lots;

    public AuctionGamerData(int key, int[] lots) {
        this.key = key;
        this.lots = lots;
    }

    public AuctionGamerData(int key) {
        this(key, new int[0]);
    }

    public int getKey() {
        return key;
    }

    public int[] getLots() {
        return lots;
    }

    public void setLots(int[] lots) {
        this.lots = lots;
    }
}
