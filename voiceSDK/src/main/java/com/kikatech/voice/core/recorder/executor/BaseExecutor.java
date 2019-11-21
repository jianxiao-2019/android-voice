package com.kikatech.voice.core.recorder.executor;

import com.kikatech.voice.util.log.LogUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

/**
 * @author SkeeterWang Created on 2018/6/4.
 */

abstract class BaseExecutor {
    private static final String TAG = "BaseExecutor";

    protected abstract boolean isLogAvailable();

    protected abstract ExecutorService getExecutor();

    ExecutorService mExecutor;

    private Map<Runnable, Future> mTasks = new HashMap<>();


    BaseExecutor() {
        mExecutor = getExecutor();
    }


    public synchronized void execute(Runnable runnable) {
        if (isLogAvailable()) {
            LogUtils.d(TAG, String.format("execute: %s", runnable));
        }

        if (mExecutor == null) {

            LogUtils.w(TAG, "invalid executor");

            return;
        }
        Future task = mExecutor.submit(runnable);
        mTasks.put(runnable, task);
    }

    public synchronized void remove(Runnable runnable) {

        LogUtils.d(TAG, String.format("remove: %s", runnable));

        if (mExecutor == null || runnable == null) {

            LogUtils.w(TAG, "invalid executor");

            return;
        }
        __cancel(runnable);
        __removeFromTaskMap(runnable);
    }

    public synchronized void cleanAll() {

        LogUtils.d(TAG, "cleanAll");

        if (mExecutor == null) {

            LogUtils.w(TAG, "invalid executor");

            return;
        }
        mExecutor.shutdown();
        for (Runnable runnable : mTasks.keySet()) {
            __cancel(runnable);
        }
        mTasks.clear();
        mExecutor = getExecutor();
    }


    private synchronized void __cancel(Runnable runnable) {

        LogUtils.d(TAG, String.format("cancel: %s", runnable));

        if (mExecutor == null || runnable == null) {

            LogUtils.w(TAG, "invalid executor");

            return;
        }
        if (runnable instanceof IRunnable) {
            IRunnable iRunnable = ((IRunnable) runnable);
            boolean isTaskRunning = iRunnable.isRunning();

            LogUtils.d(TAG, String.format("isRunning: %s", isTaskRunning));

            if (isTaskRunning) {
                iRunnable.cancel();
            } else {
                __cancelFuture(runnable);
            }
        } else {
            __cancelFuture(runnable);
        }
    }

    private synchronized void __cancelFuture(Runnable runnable) {
        Future task = mTasks.get(runnable);
        if (task != null) {
            boolean cancelled = task.cancel(true);

            LogUtils.d(TAG, String.format("try to cancel the task, cancelled: %s", cancelled));

        } else {

            LogUtils.w(TAG, "error, task not found.");

        }
    }

    private synchronized void __removeFromTaskMap(Runnable runnable) {
        if (mTasks.containsKey(runnable)) {
            mTasks.remove(runnable);
        }
    }

}
