package com.kikatech.go.util;

import android.os.Handler;
import android.os.HandlerThread;

/**
 * Created by brad_chang on 2016/1/13.
 */
public class BackgroundThread extends HandlerThread
{
    private static BackgroundThread sInstance;
    private static Handler sHandler;

    public BackgroundThread() {
        super("BackgroundThread", android.os.Process.THREAD_PRIORITY_DEFAULT);
    }

    private static void ensureThreadLocked() {
        if(sInstance == null) {
            sInstance = new BackgroundThread();
            sInstance.start();
            sHandler = new Handler( sInstance.getLooper());
        }
    }

    public static BackgroundThread get() {
        synchronized(BackgroundThread.class) {
            ensureThreadLocked();
            return sInstance;
        }
    }

    public static Handler getHandler() {
        synchronized(BackgroundThread.class) {
            ensureThreadLocked();
            return sHandler;
        }
    }

    public static void post(final Runnable runnable ) {
        synchronized(BackgroundThread.class) {
            ensureThreadLocked();
            sHandler.post(runnable);
        }
    }
}
