package com.kikatech.go.util.timer;

import android.os.Handler;
import android.os.HandlerThread;

/**
 * @author SkeeterWang Created on 2017/12/20.
 */

class TimerThread extends HandlerThread {
    private static TimerThread sIns;
    private static Handler sHandler;

    private TimerThread() {
        super("TimerThread", android.os.Process.THREAD_PRIORITY_DEFAULT);
    }

    private static void ensureThreadLocked() {
        if (sIns == null) {
            sIns = new TimerThread();
            sIns.start();
            sHandler = new Handler(sIns.getLooper());
        }
    }

    public static TimerThread get() {
        synchronized (TimerThread.class) {
            ensureThreadLocked();
            return sIns;
        }
    }

    public static Handler getHandler() {
        synchronized (TimerThread.class) {
            ensureThreadLocked();
            return sHandler;
        }
    }

    public static void post(final Runnable runnable) {
        synchronized (TimerThread.class) {
            ensureThreadLocked();
            sHandler.post(runnable);
        }
    }
}
