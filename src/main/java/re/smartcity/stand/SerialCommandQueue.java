package re.smartcity.stand;

import java.util.LinkedList;
import java.util.Queue;

public final class SerialCommandQueue {

    private final Queue<SerialCommand> queue = new LinkedList<>();

    private final Object _lock = new Object();

    public void pushCommand(SerialCommand cmd) {
        synchronized (_lock) {
            queue.remove(cmd);
            queue.offer(cmd);
        }
    }

    public void clear() {
        synchronized (_lock) {
            queue.clear();
        }
    }

    public boolean empty() {
        synchronized (_lock) {
            return queue.isEmpty();
        }
    }

    public SerialCommand poll() {
        synchronized (_lock) {
            return queue.poll();
        }
    }

}
