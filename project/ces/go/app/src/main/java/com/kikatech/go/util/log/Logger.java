package com.kikatech.go.util.log;

import android.util.Log;

import com.kikatech.go.engine.interfaces.IVoiceManager;

/**
 * Created by ryanlin on 06/10/2017.
 */

public class Logger {
    private static final boolean DEBUG = IVoiceManager.DEBUG;
    private static final String TAG = "KikaVoiceMVP";

    public static void v(String message) {
        if (DEBUG) {
            Log.v(TAG, message);
        }
    }

    public static void d(String message) {
        if (DEBUG) {
            Log.d(TAG, message);
        }
    }

    public static void i(String message) {
        Log.i(TAG, message);
    }

    public static void w(String message) {
        Log.w(TAG, message);
    }

    public static void e(String message) {
        Log.e(TAG, message);
    }
}
