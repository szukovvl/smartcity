package re.smartcity.modeling;

import lombok.AllArgsConstructor;
import lombok.Data;
import re.smartcity.energynet.component.MainSubstationPowerSystem;

import java.util.concurrent.ExecutorService;

@Data
@AllArgsConstructor
public class TaskData {
    private ExecutorService service;
    private MainSubstationPowerSystem powerSystem;
}