package re.smartcity.task;

import net.jcip.annotations.GuardedBy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import re.smartcity.data.TestBean;
import re.smartcity.handlers.SmartCityResourceHandler;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TestService {
    private final Object _sync = new Object();
    private final TestThread thread = new TestThread();
    //private final Executor executor = Executors.newSingleThreadExecutor();
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    @GuardedBy("_sync") private boolean _isShutdown;

    private TestBean bean;

    public void start(TestBean bean) {
        this.bean = bean;
        //thread.start();
        executorService.execute(thread);
    }

    public void stop() {
        synchronized (_sync) { _isShutdown = true; }
        //thread.interrupt();
        executorService.shutdown();
    }

    private class TestThread extends Thread {

        private final Logger logger = LoggerFactory.getLogger(SmartCityResourceHandler.class);

        public void run() {
            logger.info("--> поток запущен.");
            try {
                while(!isInterrupted()) {
                    sleep(5000);
                    if (bean != null) {
                        logger.info("--> поток выполняется: {}, {}", bean.getMyVal(), bean.getMyVal1());
                    } else {
                        logger.info("--> поток выполняется");
                    }
                    if (executorService.isShutdown()) {
                        logger.info("--> завершение установленно.");
                    }
                    /*synchronized (_sync) {
                        if (_isShutdown) {
                            logger.info("--> завершение установленно.");
                            break;
                        }
                    }*/
                }
            }
            catch (InterruptedException ex) {
                logger.info("--> поток прерван.");
            }
            finally {
                logger.info("--> поток завершен.");
            }
        }
    }
}
