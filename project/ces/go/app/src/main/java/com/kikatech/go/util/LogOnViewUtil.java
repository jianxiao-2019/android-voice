package com.kikatech.go.util;

import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.widget.TextView;

import com.kikatech.go.BuildConfig;
import com.kikatech.go.ui.KikaMultiDexApplication;
import com.kikatech.voice.util.log.FileLoggerUtil;

import java.util.LinkedList;

/**
 * Created by brad_chang on 2017/12/7.
 */

public class LogOnViewUtil {

    private final static int DISPLAY_LOG_COUNT = 5;
    private final static String SEPARATOR = "-------------------------------------------------";
    private final static String CLASS_SEPARATOR = "@";
    public final static String LOG_FILE = "%s_display.txt";

    static class LogInfo {
        int idx = 0;
        String log = "";
        private static StringBuilder logComposer = new StringBuilder();

        LogInfo(int i, String info) {
            idx = i;
            log = info;
        }

        public synchronized String toString() {
            logComposer.delete(0, logComposer.length());
            if (idx > 0) {
                logComposer.append(idx).append(" ");
            }
            logComposer.append(log);
            return logComposer.toString();
        }
    }


    private TextView mDebugLogView;
    private int mDisplayLogCount = DISPLAY_LOG_COUNT;
    private String mFilterClass = "";

    private final LinkedList<LogInfo> mLogLinkedList;
    private int mLogCount = 1;
    private int mFileLoggerId = -1;

    private static LogOnViewUtil sLogOnViewUtil;

    private LogOnViewUtil() {
        mLogLinkedList = new LinkedList<>();

        mFileLoggerId = FileLoggerUtil.getIns().configFileLogger(LogUtil.LOG_FOLDER, LOG_FILE);

        FileLoggerUtil.getIns().writeLogToFile(mFileLoggerId, "App Version : " + BuildConfig.VERSION_NAME + " / " + BuildConfig.VERSION_CODE);
        FileLoggerUtil.getIns().writeLogToFile(mFileLoggerId,
                DeviceUtil.getBrand() + " / " + DeviceUtil.getManufacturer() + " / " +
                        DeviceUtil.getModel() + " / " + DeviceUtil.getAndroidID(KikaMultiDexApplication.getAppContext()));
        FileLoggerUtil.getIns().writeLogToFile(mFileLoggerId, "=====================================================");
    }

    public synchronized static LogOnViewUtil getIns() {
        if (sLogOnViewUtil == null) {
            sLogOnViewUtil = new LogOnViewUtil();
        }
        return sLogOnViewUtil;
    }

    public LogOnViewUtil configViews(TextView logsView) {
        mDebugLogView = logsView;
        return sLogOnViewUtil;
    }

    public LogOnViewUtil configDisplayLogCount(int logCount) {
        mDisplayLogCount = logCount;
        return sLogOnViewUtil;
    }

    public LogOnViewUtil configFilterClass(String className) {
        mFilterClass = className;
        return sLogOnViewUtil;
    }

    public synchronized void addSeparator() {
        addLog(-1, SEPARATOR);
        updateLogView();
    }

    public synchronized void addLog(@NonNull String logType, String detail) {

        String detailLog = orgClassStringLog(detail);
        String info = logType;
        if (!TextUtils.isEmpty(detailLog)) {
            info += " " + detailLog;
        }

        if (info.length() == 0) {
            info = "<empty>";
        }

        while (mLogLinkedList.size() > mDisplayLogCount) {
            mLogLinkedList.removeFirst();
        }
        addLog(mLogCount++, info);

        updateLogView();
    }

    private void addLog(int logCount, String log) {
        LogInfo li = new LogInfo(logCount, log);
        mLogLinkedList.add(li);
        FileLoggerUtil.getIns().writeLogToFile(mFileLoggerId, li.toString());
    }

    private String orgClassStringLog(String log) {
        if (!TextUtils.isEmpty(mFilterClass) && log.contains(mFilterClass)) {
            String[] ret = log.split(mFilterClass);
            if (ret.length > 1) {
                return ret[1].split(CLASS_SEPARATOR)[0];
            }
        }
        return log;
    }

    private void updateLogView() {
        final StringBuilder log = new StringBuilder();
        int size = mLogLinkedList.size();
        for (int i = 0; i < size; i++) {
            LogInfo li = mLogLinkedList.get(i);
            log.append(li.toString()).append("\n");
        }

        mDebugLogView.post(new Runnable() {
            @Override
            public void run() {
                mDebugLogView.setText(log.toString());
            }
        });
    }
}