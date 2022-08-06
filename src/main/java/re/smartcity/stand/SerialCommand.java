package re.smartcity.stand;

import java.nio.ByteBuffer;

public final class SerialCommand {

    private byte address;
    private byte pack;
    private int val;

    public SerialCommand(byte packType) { // для широковещательного адреса
        this(SerialElementAddresses.BROADCAST_ADDRESS, packType, 0);
    }

    public SerialCommand(byte packType, int val) { // для широковещательного адреса
        this(SerialElementAddresses.BROADCAST_ADDRESS, packType, val);
    }

    public SerialCommand(byte elAddress, byte packType) {
        this(elAddress, packType, 0);
    }

    public SerialCommand(byte elAddress, byte packType, int val) {
        this.address = elAddress;
        this.pack = packType;
        this.val = val;
    }

    @Override
    public boolean equals(Object obj) {
        // !!! ВНИАМЕНИЕ: проверяю только временные точки
        if (obj == this) { return true; }
        if (obj == null) { return false; }
        if (!(obj instanceof SerialCommand)) { return false; }
        return this.address == ((SerialCommand) obj).address && this.pack == ((SerialCommand) obj).pack;
    }

    @Override
    public int hashCode() {
        // !!! ВНИМАНИЕ: использую только адрес и тип пакета
        return ByteBuffer.wrap(new byte[] { this.address, this.pack }).getShort();
    }

    @Override
    public String toString() {
        return String.format("%02X %02X: %d", this.address, this.pack, this.val);
    }

    public byte getAddress() {
        return address;
    }

    public byte getPack() {
        return pack;
    }

    public int getVal() {
        return val;
    }
}
