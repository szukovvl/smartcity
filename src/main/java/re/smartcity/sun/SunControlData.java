package re.smartcity.sun;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import re.smartcity.wind.WindControlCommand;

import java.util.concurrent.ConcurrentLinkedQueue;

public class SunControlData {

    private final Logger logger = LoggerFactory.getLogger(SunControlData.class);
    volatile private Integer waiting = 500; // ожидание между опросом ПЛК
    volatile private Integer restartingWait = 3000; // ожидание при перезапуске, после останова сервиса

    private final ConcurrentLinkedQueue<WindControlCommand> commands = new ConcurrentLinkedQueue<WindControlCommand>();

    public Integer getWaiting() {
        return waiting;
    }

    public Integer getRestartingWait() {
        return restartingWait;
    }

    public void addCommand(WindControlCommand command) {
        logger.info("запрос на выполнение: {}", command);
        while (commands.remove(command)) {
            logger.info("поглощение подобной команды");
        }
        commands.offer(command);
    }

    public boolean commandExists() {
        return !commands.isEmpty();
    }

    public WindControlCommand currentCommand() {
        return commands.poll();
    }
}