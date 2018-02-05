package com.kikatech.go.util;

import android.support.annotation.NonNull;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author wangskeeter Created on 16/8/24.
 */

public class AsyncThreadPool {
    private static final String TAG = "AsyncThreadPool";

    private static AsyncThreadPool sIns;

    private ScheduledThreadPoolExecutor mExecutor;

    private Map<Runnable, ScheduledFuture> mTasks = new HashMap<>();

    public static synchronized AsyncThreadPool getIns() {
        if (sIns == null) {
            sIns = new AsyncThreadPool();
        }
        return sIns;
    }

    private AsyncThreadPool() {
        mExecutor = new ScheduledThreadPoolExecutor(2, new ScheduledThreadPoolExecutor.DiscardOldestPolicy());
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
            if (LogUtil.DEBUG) {
                printTaskStatus(task);
            }
            boolean removed = mExecutor.remove(runnable);
            if (LogUtil.DEBUG) {
                LogUtil.log(TAG, String.format("try to remove the task first, removed: %s", removed));
            }
            if (!removed) {
                boolean cancelled = task.cancel(true);
                if (LogUtil.DEBUG) {
                    LogUtil.log(TAG, String.format("try to cancel the task, cancelled: %s", cancelled));
                }
                if (cancelled) {
                    if (LogUtil.DEBUG) {
                        LogUtil.log(TAG, "canceled, try to purge");
                    }
                    mExecutor.purge();
                }
            }
            mTasks.remove(runnable);
        } else {
            if (LogUtil.DEBUG) {
                LogUtil.logw(TAG, "error, task not found.");
            }
        }
    }


    private void printTaskStatus(@NonNull ScheduledFuture task) {
        if (LogUtil.DEBUG) {
            LogUtil.logd(TAG, String.format("[task] isDone: %s", task.isDone()));
            LogUtil.logd(TAG, String.format("[task] isCancelled: %s", task.isCancelled()));
        }
    }
}
