package com.kikatech.voice.util.log;

import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import java.util.Locale;

/**
 * Created by brad_chang on 2016/1/10.
 */
public class Logger {
    public static boolean DEBUG = false;
    private static boolean sIsFileLogEnabled = DEBUG;

    public static final String TAG = "KikaVoiceSdk";
    private static final int DEF_STACKS_COUNT = 0;

    private static int sPid = 0;
    private static final int PARENT_NODE = 3, SELF_NODE = 2;
    private static final String PARENT_LOG_FORMAT = "[%s:%s:ln%d] ";
    private static final String LOG_FORMAT = "[%s:%s:ln%d] %s (pid: %d)";

    public final static String LOG_FOLDER = "kikaVoiceSdk/log";
    public final static String LOG_FILE = "%s_voice_sdk.txt";
    private static int mFileLoggerId = -1;

    public static void updateDebugState(boolean isDebug) {
        DEBUG = isDebug;
        sIsFileLogEnabled = isDebug;

        sPid = DEBUG ? 0 : android.os.Process.myPid();
    }

    private enum LogLabel {
        VERBOSE,
        DEBUG,
        INFO,
        WARN,
        ERROR,
        ASSERT,
        EXCEPTION
    }

    private static String getStackMsg(String log, int parentStacksCount) {
        String message = log;
        try {
            StackTraceElement[] stacks = (new Throwable()).getStackTrace();
            String parentStack = "";
            String className = null;
            String methodName = null;
            int lineNumber = 0;
            StackTraceElement stack;
            String[] classes;
            if (parentStacksCount > 0) {
                if (stacks.length >= PARENT_NODE) {
                    for (int i = PARENT_NODE; i <= PARENT_NODE + parentStacksCount && i < stacks.length; i++) {
                        stack = stacks[i];
                        className = stack.getClassName();
                        classes = className.split("\\.");
                        if (classes.length > 0) className = classes[classes.length - 1];
                        methodName = stack.getMethodName();
                        lineNumber = stack.getLineNumber();

                        parentStack += String.format(Locale.ENGLISH, PARENT_LOG_FORMAT, className, methodName, lineNumber);
                    }
                }
            }
            if (stacks.length >= SELF_NODE) {
                stack = stacks[SELF_NODE];
                className = stack.getClassName();
                classes = className.split("\\.");
                if (classes.length > 0) className = classes[classes.length - 1];
                methodName = stack.getMethodName();
                lineNumber = stack.getLineNumber();
            }

            message = String.format(Locale.ENGLISH, LOG_FORMAT, className, methodName, lineNumber, log, sPid);
            if (!TextUtils.isEmpty(parentStack)) message += ("\n parent stacks: " + parentStack);
            return message;
        } catch (Exception ignore) {
        }
        return message;
    }

    private static void log(LogLabel logLabel, String logTag, String log) {
        log(logLabel, logTag, log, null);
    }

    private static void log(LogLabel logLabel, String logTag, String log, Throwable throwable) {
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
            case EXCEPTION:
                Log.e(logTag, log, throwable);
                break;
        }

        if (sIsFileLogEnabled) {
            if (mFileLoggerId == -1) {
                mFileLoggerId = FileLoggerUtil.getIns().configFileLogger(LOG_FOLDER, LOG_FILE);
            }
            FileLoggerUtil.getIns().writeLogToFile(mFileLoggerId, logTag + " " + log);
        }
    }

    public static void v(@NonNull String log) {
        if (DEBUG) log(LogLabel.VERBOSE, TAG, getStackMsg(log, DEF_STACKS_COUNT));
    }

    public static void d(@NonNull String log) {
        if (DEBUG) log(LogLabel.DEBUG, TAG, getStackMsg(log, DEF_STACKS_COUNT));
    }

    public static void i(@NonNull String log) {
        log(LogLabel.INFO, TAG, getStackMsg(log, DEF_STACKS_COUNT));
    }

    public static void w(@NonNull String log) {
        log(LogLabel.WARN, TAG, getStackMsg(log, DEF_STACKS_COUNT));
    }

    public static void e(@NonNull String log) {
        log(LogLabel.ERROR, TAG, getStackMsg(log, DEF_STACKS_COUNT));
    }

    public static void wtf(@NonNull String log) {
        log(LogLabel.ASSERT, TAG, getStackMsg(log, DEF_STACKS_COUNT));
    }

    public static void v(@NonNull String logTag, @NonNull String log) {
        if (DEBUG) log(LogLabel.VERBOSE, logTag, getStackMsg(log, 0));
    }

    public static void d(@NonNull String logTag, @NonNull String log) {
        if (DEBUG) log(LogLabel.DEBUG, logTag, getStackMsg(log, 0));
    }

    public static void i(@NonNull String logTag, @NonNull String log) {
        log(LogLabel.INFO, logTag, getStackMsg(log, DEF_STACKS_COUNT));
    }

    public static void w(@NonNull String logTag, @NonNull String log) {
        log(LogLabel.WARN, logTag, getStackMsg(log, 0));
    }

    public static void e(@NonNull String logTag, @NonNull String log) {
        log(LogLabel.ERROR, logTag, getStackMsg(log, 0));
    }

    public static void wtf(@NonNull String logTag, @NonNull String log) {
        log(LogLabel.ASSERT, logTag, getStackMsg(log, 0));
    }

    public static void v(@NonNull String logTag, @NonNull String log, int parentStacksCount) {
        if (DEBUG) log(LogLabel.VERBOSE, logTag, getStackMsg(log, parentStacksCount));
    }

    public static void d(@NonNull String logTag, @NonNull String log, int parentStacksCount) {
        if (DEBUG) log(LogLabel.DEBUG, logTag, getStackMsg(log, parentStacksCount));
    }

    public static void i(@NonNull String logTag, @NonNull String log, int parentStacksCount) {
        log(LogLabel.INFO, logTag, getStackMsg(log, parentStacksCount));
    }

    public static void w(@NonNull String logTag, @NonNull String log, int parentStacksCount) {
        log(LogLabel.WARN, logTag, getStackMsg(log, parentStacksCount));
    }

    public static void e(@NonNull String logTag, @NonNull String log, int parentStacksCount) {
        log(LogLabel.ERROR, logTag, getStackMsg(log, parentStacksCount));
    }

    public static void wtf(@NonNull String logTag, @NonNull String log, int parentStacksCount) {
        log(LogLabel.ASSERT, logTag, getStackMsg(log, parentStacksCount));
    }

    public static void printStackTrace(@NonNull String logTag, @NonNull String log, Throwable throwable) {
        if (DEBUG) log(LogLabel.EXCEPTION, logTag, getStackMsg(log, 0), throwable);
    }
}
