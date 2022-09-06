package re.smartcity.modeling;

import lombok.AllArgsConstructor;
import lombok.Data;
import re.smartcity.energynet.component.MainSubstationPowerSystem;
import re.smartcity.modeling.data.GamerScenesData;

import java.util.concurrent.ExecutorService;

@Data
@AllArgsConstructor
public class TaskData {
    private ExecutorService service;
    private MainSubstationPowerSystem powerSystem;
    private GamerScenesData scenesData;
    private int[] choicesScene; // !!! пока а костылях
}
