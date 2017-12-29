package com.kikatech.voice.util;

import android.os.Handler;
import android.os.HandlerThread;

/**
 * Created by brad_chang on 2017/12/15.
 */

public class BackgroundThread extends HandlerThread {
    private static BackgroundThread sInstance;
    private static Handler sHandler;

    public BackgroundThread() {
        super("voice::b-thread", android.os.Process.THREAD_PRIORITY_DEFAULT);
    }

    private static void ensureThreadLocked() {
        if (sInstance == null) {
            sInstance = new BackgroundThread();
            sInstance.start();
            sHandler = new Handler(sInstance.getLooper());
        }
    }

    public static void post(final Runnable runnable) {
        synchronized (BackgroundThread.class) {
            ensureThreadLocked();
            sHandler.post(runnable);
        }
    }

    public static boolean postDelayed(Runnable r, long delayMillis) {
        synchronized (BackgroundThread.class) {
            ensureThreadLocked();
            return sHandler.postDelayed(r, delayMillis);
        }
    }
}