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

    private enum LogLabel {
        VERBOSE,
        DEBUG,
        INFO,
        WARN,
        ERROR,
        ASSERT
    }

    public static void v(String message) {
        if (DEBUG) {
            log(LogLabel.VERBOSE, TAG, message);
        }
    }

    public static void d(String message) {
        if (DEBUG) {
            log(LogLabel.DEBUG, TAG, message);
        }
    }

    public static void i(String message) {
        if (DEBUG) {
            log(LogLabel.INFO, TAG, message);
        }
    }

    public static void w(String message) {
        if (DEBUG) {
            log(LogLabel.WARN, TAG, message);
        }
    }

    public static void e(String message) {
        if (DEBUG) {
            log(LogLabel.ERROR, TAG, message);
        }
    }

    private static void log(LogLabel logLabel, String logTag, String oriLog) {
        if(oriLog == null) {
            oriLog = "<err><null>";
        }
        String log = "[mvp]" + oriLog;
        switch (logLabel) {
            case VERBOSE:
                Log.v(logTag, log);
                break;
            case DEBUG:
                Log.d(logTag, log);
                break;
            case INFO:
                Log.i(logTag, log);
                break;
            case WARN:
                Log.w(logTag, log);
                break;
            case ERROR:
                Log.e(logTag, log);
                break;
            case ASSERT:
                Log.wtf(logTag, log);
                break;
        }

        if (ENABLE_FILE_LOG) {
            if (mFileLoggerId == -1) {
                mFileLoggerId = FileLoggerUtil.getIns().configFileLogger(LOG_FOLDER, LOG_FILE);
            }
            FileLoggerUtil.getIns().writeLogToFile(mFileLoggerId, log);
        }
    }
}
