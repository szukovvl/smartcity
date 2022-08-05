package re.smartcity.stand;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StandControlData {

    public static int DELAY_WHEN_EMPTY = 100;
    public static int DELAY_COMMAND_FLOW = 5;

    volatile private Integer restartingWait = 3000; // ожидание при перезапуске, после останова сервиса

    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    volatile private String port; // порт подключения блока управления

    public void apply(StandControlData src) {
        setRestartingWait(src.getRestartingWait());
        setPort(src.getPort());
    }

}
