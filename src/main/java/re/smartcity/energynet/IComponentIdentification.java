package re.smartcity.energynet;

public interface IComponentIdentification {

    long getId();
    String getIdenty();
    byte getDevaddr();
    SupportedTypes getComponentType();
    boolean itIsMine(int address); // определяет принадлежность адреса данному устройству
}
