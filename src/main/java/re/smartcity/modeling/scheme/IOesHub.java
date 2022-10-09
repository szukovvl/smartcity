package re.smartcity.modeling.scheme;

import re.smartcity.energynet.IComponentIdentification;

public interface IOesHub {
    boolean hasOwner();
    IComponentIdentification getOwner();
    int getAddress();
    boolean supportControlPort();
    IConnectionPort getControlPort();
    boolean supportInputs();
    IConnectionPort[] getInputs();
    boolean supportOutputs();
    IConnectionPort[] getOutputs();
    boolean hasError();
    String getError();
    void setError(String error);
    boolean itIsMine(int address); // определяет принадлежность адреса данному устройству
    IConnectionPort connectionByAddress(int address);
}
