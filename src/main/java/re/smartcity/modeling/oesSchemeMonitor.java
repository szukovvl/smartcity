package re.smartcity.modeling;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class oesSchemeMonitor implements Runnable {

    private final Logger logger = LoggerFactory.getLogger(oesSchemeMonitor.class);

    private final Object _syncThread;

    public oesSchemeMonitor(Object syncThread) {
        this._syncThread = syncThread;
    }

    @Override
    public void run() {
        int errorCount = 0;
        while (true) {
            try {
                synchronized (_syncThread) {
                    _syncThread.wait();
                }
                logger.info("+++");
                errorCount = 0;
            }
            catch (InterruptedException ignored) {
                return;
            }
            catch (Exception ex) {
                logger.error(ex.getMessage());
                errorCount++;
                if (errorCount > 5) {
                    errorCount = 0;
                    logger.warn("Любое действие ведет к ошибке");
                }
            }
        }
    }
}
