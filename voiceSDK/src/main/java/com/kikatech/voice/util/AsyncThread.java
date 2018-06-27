package com.kikatech.voice.util;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author wangskeeter Created on 16/8/24.
 */
public class AsyncThread {

    private static final int POOL_SIZE = 2;
    private static AsyncThread sIns;

    private ScheduledThreadPoolExecutor mExecutor;

    private Map<Runnable, ScheduledFuture> mTasks = new HashMap<>();

    public static synchronized AsyncThread getIns() {
        if (sIns == null) {
            sIns = new AsyncThread();
        }
        return sIns;
    }

    private AsyncThread() {
        mExecutor = new ScheduledThreadPoolExecutor(POOL_SIZE, new ScheduledThreadPoolExecutor.DiscardOldestPolicy());
    }


    public synchronized void execute(Runnable runnable) {
        executeDelay(runnable, 0);
    }

    public synchronized void executeDelay(Runnable runnable, long delay) {
        ScheduledFuture task = mExecutor.schedule(runnable, delay, TimeUnit.MILLISECONDS);
        mTasks.put(runnable, task);
    }

    public synchronized void remove(Runnable runnable) {
        ScheduledFuture task = mTasks.get(runnable);
        if (task != null) {
            boolean removed = mExecutor.remove(runnable);
            if (!removed) {
                boolean cancelled = task.cancel(true);
                if (cancelled) {
                    mExecutor.purge();
                }
            }
            mTasks.remove(runnable);
        }
    }

    public synchronized boolean isBusy() {
        return mExecutor.getQueue().size() > 0;
    }
}
