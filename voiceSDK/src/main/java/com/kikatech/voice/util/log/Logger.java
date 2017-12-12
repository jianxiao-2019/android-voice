package com.kikatech.voice.util.log;

import android.util.Log;

/**
 * Created by ryanlin on 06/10/2017.
 */

public class Logger {
    public static final boolean DEBUG = true;
    private final static boolean ENABLE_FILE_LOG = DEBUG;
    private static final String TAG = "KikaVoiceMVP";

    private final static String LOG_FOLDER = LogUtil.LOG_FOLDER;
    public final static String LOG_FILE = "%s_voice_mvp.txt";
    private static int mFileLoggerId = -1;

    public static void v(String message) {
        if (DEBUG) {
            Log.v(TAG, message);
            writeFileLog(message);
        }
    }

    public static void d(String message) {
        if (DEBUG) {
            Log.d(TAG, message);
            writeFileLog(message);
        }
    }

    public static void i(String message) {
        if (DEBUG) {
            Log.i(TAG, message);
            writeFileLog(message);
        }
    }

    public static void w(String message) {
        if (DEBUG) {
            Log.w(TAG, message);
            writeFileLog(message);
        }
    }

    public static void e(String message) {
        if (DEBUG) {
            Log.e(TAG, message);
            writeFileLog(message);
        }
    }

    private static void writeFileLog(String message) {
        if (ENABLE_FILE_LOG) {
            if (mFileLoggerId == -1) {
                mFileLoggerId = FileLoggerUtil.getIns().configFileLogger(LOG_FOLDER, LOG_FILE);
            }
            FileLoggerUtil.getIns().writeLogToFile(mFileLoggerId, message);
        }
    }
}
