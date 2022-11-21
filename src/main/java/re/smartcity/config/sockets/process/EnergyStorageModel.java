package re.smartcity.config.sockets.process;

import re.smartcity.energynet.GenerationUsageModes;
import re.smartcity.energynet.IComponentIdentification;
import re.smartcity.energynet.SupportedTypes;
import re.smartcity.energynet.component.EnergyStorage;

public class EnergyStorageModel {

    private final EnergyStorage storage;

    private EnergyStorageModel(EnergyStorage storage) {
        this.storage = storage;
    }

    private void initialState() {

    }

    public double getEnergy() {
        return storage.getData().getEnergy();
    }

    public boolean isReservationMode() {
        return storage.getData().getMode() == GenerationUsageModes.RESERVE;
    }

    public static EnergyStorageModel create(IComponentIdentification oes) {
        return oes.getComponentType() == SupportedTypes.STORAGE
                ? new EnergyStorageModel((EnergyStorage) oes)
                : null;
    }
}
