package com.kikatech.go.util.timer;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author SkeeterWang Created on 2017/12/20.
 */

class TimerThread {
    private static TimerThread sIns;

    private ScheduledThreadPoolExecutor mExecutor;

    static synchronized TimerThread getIns() {
        if (sIns == null) {
            sIns = new TimerThread();
        }
        return sIns;
    }

    private TimerThread() {
        mExecutor = new ScheduledThreadPoolExecutor(1, new ScheduledThreadPoolExecutor.DiscardOldestPolicy());
    }

    void execute(Runnable runnable) {
        mExecutor.execute(runnable);
    }

    void executeDelay(Runnable runnable, long delay) {
        mExecutor.schedule(runnable, delay, TimeUnit.MILLISECONDS);
    }

    void remove(Runnable runnable) {
        mExecutor.remove(runnable);
    }
}
