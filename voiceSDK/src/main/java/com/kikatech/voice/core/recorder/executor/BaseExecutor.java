package com.kikatech.voice.core.recorder.executor;

import com.kikatech.voice.util.log.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

/**
 * @author SkeeterWang Created on 2018/6/4.
 */

abstract class BaseExecutor {
    private static final String TAG = "BaseExecutor";

    protected abstract ExecutorService getExecutor();

    ExecutorService mExecutor;

    private Map<IRunnable, Future> mTasks = new HashMap<>();

    BaseExecutor() {
        mExecutor = getExecutor();
    }

    public synchronized void execute(IRunnable runnable) {
        if (Logger.DEBUG) {
            Logger.d(TAG, String.format("execute: %s", runnable));
        }
        if (mExecutor == null) {
            if (Logger.DEBUG) {
                Logger.w(TAG, "invalid executor");
            }
            return;
        }
        Future task = mExecutor.submit(runnable);
        mTasks.put(runnable, task);
    }

    public synchronized void remove(IRunnable runnable) {
        if (Logger.DEBUG) {
            Logger.d(TAG, String.format("remove: %s", runnable));
        }
        if (mExecutor == null || runnable == null) {
            if (Logger.DEBUG) {
                Logger.w(TAG, "invalid executor");
            }
            return;
        }
        cancel(runnable);
        if (mTasks.containsKey(runnable)) {
            mTasks.remove(runnable);
        }
    }

    private synchronized void cancel(IRunnable runnable) {
        if (Logger.DEBUG) {
            Logger.d(TAG, String.format("cancel: %s", runnable));
        }
        if (mExecutor == null || runnable == null) {
            if (Logger.DEBUG) {
                Logger.w(TAG, "invalid executor");
            }
            return;
        }
        boolean isTaskRunning = runnable.isRunning();
        if (Logger.DEBUG) {
            Logger.d(TAG, String.format("isRunning: %s", isTaskRunning));
        }
        if (isTaskRunning) {
            runnable.cancel();
        } else {
            Future task = mTasks.get(runnable);
            if (task != null) {
                boolean cancelled = task.cancel(true);
                if (Logger.DEBUG) {
                    Logger.d(TAG, String.format("try to cancel the task, cancelled: %s", cancelled));
                }
            } else {
                if (Logger.DEBUG) {
                    Logger.w(TAG, "error, task not found.");
                }
            }
        }
    }

    public synchronized void cleanAll() {
        if (Logger.DEBUG) {
            Logger.d(TAG, "cleanAll");
        }
        if (mExecutor == null) {
            if (Logger.DEBUG) {
                Logger.w(TAG, "invalid executor");
            }
            return;
        }
        mExecutor.shutdown();
        for (IRunnable runnable : mTasks.keySet()) {
            cancel(runnable);
        }
        mTasks.clear();
        mExecutor = getExecutor();
    }
}
