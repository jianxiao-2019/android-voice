package com.kikatech.voicesdktester.utils;

import android.os.Environment;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * @author SkeeterWang Created on 2017/10/25.
 */

public class FileUtil {
    private static final String TAG = "FileUtil";
    
    private static final String FOLDER_RECORD = "/kikaVoiceSDK";
    private static final String FOLDER_AUDIO = "/voiceTester";

    public static String getCurrentTimeFormattedFileName() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss", new Locale("en"));
        Date resultDate = new Date(System.currentTimeMillis());
        return sdf.format(resultDate);
    }


    public static String getAudioFilePath(String fileName) {
        File file = new File(getAudioFolder(), fileName);
        return file.getAbsolutePath();
    }


    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static String getAudioFolder() {
        String filePath = getRootRecordFolder();
        File file = new File(filePath, FOLDER_AUDIO);
        if (!file.exists()) {
            file.mkdirs();
        }
        return file.getAbsolutePath();
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private static String getRootRecordFolder() {
        String folder = Environment.getExternalStorageDirectory().getPath();
        File file = new File(folder, FOLDER_RECORD);
        if (!file.exists()) {
            file.mkdirs();
        }
        return file.getAbsolutePath();
    }
}