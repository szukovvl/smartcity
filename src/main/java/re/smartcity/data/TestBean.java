package re.smartcity.data;

import net.jcip.annotations.GuardedBy;
import net.jcip.annotations.ThreadSafe;

import java.util.concurrent.ExecutorService;

@ThreadSafe // это просто аннотация, не реализующая потокобезопасность
public class TestBean {
    private volatile String myVal;
    @GuardedBy("this") private String myVal1;

    public String getMyVal() {
        return myVal;
    }

    public void setMyVal(String myVal) {
        this.myVal = myVal;
    }

    public synchronized String getMyVal1() {
        return myVal1;
    }

    public synchronized void setMyVal1(String myVal1) {
        this.myVal1 = myVal1;
    }
}
