package com.kikatech.go.util.storage;

import com.kikatech.go.util.BackgroundThread;
import com.kikatech.go.util.CalendarUtil;
import com.kikatech.go.util.LogUtil;
import com.kikatech.go.util.preference.GlobalPref;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * @author SkeeterWang Created on 2018/6/29.
 */
public class AudioFileUtil {
    private static final String TAG = "AudioFileUtil";

    private static final long FILE_ALIVE_DAYS = LogUtil.DEBUG ? -1 : 7;

    public static String getCurrentTimeFormattedFileName() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss", new Locale("en"));
        Date resultDate = new Date(System.currentTimeMillis());
        return sdf.format(resultDate);
    }

    public static synchronized void checkFiles() {
        long lastCheckedTime = GlobalPref.getIns().getCheckAudioFileTime();
        long millisecond = (System.currentTimeMillis() - lastCheckedTime);
        boolean shouldCheckFile = lastCheckedTime == 0 || millisecond >= 24 * 60 * 60 * 1000;
        if (LogUtil.DEBUG) {
            LogUtil.logd(TAG, String.format("Last check time is %s ms ago.", millisecond));
            LogUtil.logd(TAG, String.format("Should Check And Delete Files? %s", shouldCheckFile));
        }
        if (shouldCheckFile) {
            checkAndDeleteFile(FileUtil.getAudioFolder(), FILE_ALIVE_DAYS);
            GlobalPref.getIns().setCheckAudioFileTime(System.currentTimeMillis());
        }
    }

    private static void checkAndDeleteFile(String path, long aliveDays) {
        __checkAndDeleteFile(new File(path), aliveDays);
    }

    private static void __checkAndDeleteFile(final File dir, final long aliveDays) {
        BackgroundThread.post(new Runnable() {
            @Override
            public void run() {
                if (aliveDays < 0) {
                    return;
                }
                File[] files = FileUtil.listFiles(dir);
                if (files == null) {
                    return;
                }
                for (File file : files) {
                    long daysBetween = CalendarUtil.daysBetweenTodayAnd(file.lastModified());
                    if (daysBetween >= aliveDays) {
                        boolean isDeleted = file.delete();
                        if (LogUtil.DEBUG) {
                            LogUtil.logv(TAG, String.format("__checkAndDeleteFile, isDeleted: %1$s, file: %2$s", isDeleted, file.getAbsolutePath()));
                        }
                    }
                }
            }
        });
    }

}
