package re.smartcity.modeling.scheme;

import re.smartcity.energynet.IComponentIdentification;

public interface IControlHub {

    int getDevaddr();
    IComponentIdentification getLinkedOes();
    boolean isOff();
    void setOff(boolean off);
    String getErrorMsg();
    void setErrorMsg(String msg);

}
