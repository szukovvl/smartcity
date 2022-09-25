package re.smartcity.config.sockets.model;

public class ResponseSchemeDataBuilder {

    private final ResponseSchemeData data = new ResponseSchemeData();
    public ResponseSchemeDataBuilder(int key) { data.setGamerkey(key); }

    public ResponseSchemeData build() {
        return this.data;
    }

    public ResponseSchemeDataBuilder substation(int key) {
        this.data.setSubstation(key);
        return this;
    }

    public ResponseSchemeDataBuilder consumers(int[] keys) {
        this.data.setConsumers(keys);
        return this;
    }

    public ResponseSchemeDataBuilder tcconsumers(int[] keys) {
        this.data.setTcconsumers(keys);
        return this;
    }

    public ResponseSchemeDataBuilder tcprice(double price) {
        this.data.setTcprice(price);
        return this;
    }

    public ResponseSchemeDataBuilder generators(PurchasedLot[] items) {
        this.data.setGenerators(items);
        return this;
    }
}
