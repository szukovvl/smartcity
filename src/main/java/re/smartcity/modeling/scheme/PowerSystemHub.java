package re.smartcity.modeling.scheme;

import com.fasterxml.jackson.annotation.JsonIgnore;
import re.smartcity.modeling.TaskData;

import java.util.Arrays;

import static re.smartcity.stand.SerialElementAddresses.*;

public final class PowerSystemHub implements IControlHub {

    @JsonIgnore
    private final TaskData task;
    private final int ctrladdr; // линия управления
    private final int devaddr; // адрес устройства
    private boolean off = false; // объект отключен

    private final SubnetHub[] inputs;
    private final SubnetHub[] outputs;

    public PowerSystemHub(TaskData task) {
        this.task = task;
        this.ctrladdr = task.getPowerSystem().getDevaddr() == MAIN_SUBSTATION_1
            ? MAIN_SUBSTATION_1_CONNECTOR_1 : MAIN_SUBSTATION_2_CONNECTOR_1;
        this.devaddr = task.getPowerSystem().getDevaddr();
        this.inputs = Arrays.stream(task.getPowerSystem().getData().getInputs())
                .map(SubnetHub::new)
                .toArray(SubnetHub[]::new);
        this.outputs = Arrays.stream(task.getPowerSystem().getData().getOutputs())
                .map(SubnetHub::new)
                .toArray(SubnetHub[]::new);
    }

    public TaskData getTask() {
        return task;
    }

    public int getCtrladdr() {
        return ctrladdr;
    }

    public SubnetHub[] getInputs() {
        return inputs;
    }

    public SubnetHub[] getOutputs() {
        return outputs;
    }

    //region IControlHub
    @Override
    public int getDevaddr() {
        return devaddr;
    }

    @Override
    public boolean isOff() {
        return off;
    }

    @Override
    public void setOff(boolean off) {
        this.off = off;
    }
    //endregion
}
