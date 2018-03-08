package com.kikatech.voice.core.debug;

import android.os.Environment;
import android.text.TextUtils;
import android.util.LongSparseArray;
import android.util.Pair;

import com.kikatech.voice.core.recorder.IVoiceSource;
import com.kikatech.voice.core.webservice.message.EditTextMessage;
import com.kikatech.voice.core.webservice.message.IntermediateMessage;
import com.kikatech.voice.core.webservice.message.Message;
import com.kikatech.voice.core.webservice.message.TextMessage;
import com.kikatech.voice.service.VoiceConfiguration;
import com.kikatech.voice.util.log.Logger;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Created by ryanlin on 12/02/2018.
 */

public class DebugUtil {

    private static final String PRE_PHONE   = "Phone_";
    private static final String PRE_KIKA_GO = "Kikago_";
    private static final String PRE_LOCAL   = "Local_";
    private static final String PRE_UNKNOWN = "Unknown_";

    private static boolean sIsDebug = false;

    private static String sDebugFilepath;
    private static File sCacheDir;

    private static LongSparseArray<String> cidToFilePath = new LongSparseArray<>();

    public static void updateCacheDir(VoiceConfiguration conf) {
        sIsDebug = conf.getIsDebugMode();
        if (!sIsDebug) {
            return;
        }

        sCacheDir = getCacheDir(conf);
        if (sCacheDir == null) {
            sIsDebug = false;
            Logger.i("updateCacheDir sCacheDir == null");
        }
        cidToFilePath.clear();
    }

    public static boolean isDebug () {
        return sIsDebug;
    }

    public static void updateDebugInfo(VoiceConfiguration conf) {
        sDebugFilepath = null;
        if (!sIsDebug) {
            return;
        }

        if (sCacheDir != null) {
            sDebugFilepath = sCacheDir.getPath() + "/" + getFilePrefix(conf) + getCurrentTimeFormatted();
        }
        Logger.i("updateCacheDir sDebugFilepath = " + sDebugFilepath);
    }

    public static String getDebugFilePath() {
        return sDebugFilepath;
    }

    public static String getDebugFolderPath() {
        if (sCacheDir == null) {
            return null;
        }
        return sCacheDir.getPath() + "/";
    }

    private static String getFilePrefix(VoiceConfiguration conf) {
        IVoiceSource source =  conf.getVoiceSource();
        if (source == null) {
            return PRE_PHONE;
        } else if (source.getClass().getSimpleName().contains("Usb")) {
            return PRE_KIKA_GO;
        } else if (source.getClass().getSimpleName().contains("Local")) {
            return PRE_LOCAL;
        }
        return PRE_UNKNOWN;
    }

    private static String getCurrentTimeFormatted() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss", new Locale("en"));
        Date resultDate = new Date(System.currentTimeMillis());

        return sdf.format(resultDate);
    }

    private static File getCacheDir(VoiceConfiguration conf) {
        try {
            File file = new File(Environment.getExternalStorageDirectory() + "/kikaVoiceSDK/" + conf.getDebugFileTag() + "/");
            createFolderIfNecessary(file);
            return file;
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return null;
    }

    private static boolean createFolderIfNecessary(File folder) {
        if (folder != null) {
            if (!folder.exists() || !folder.isDirectory()) {
                return folder.mkdirs();
            }
            return true;
        }
        return false;
    }

    // TODO : 測試
    public static boolean convertCurrentPcmToWav() {
        return addWavHeader(sDebugFilepath);
    }

    private static boolean addWavHeader(String debugFilePath) {
        // TODO : convert at the other thread.
        Logger.i("-----addWavHeader mDebugFileName = " + debugFilePath);
        if (TextUtils.isEmpty(debugFilePath)) {
            return false;
        }
        String fileName = debugFilePath.substring(debugFilePath.lastIndexOf("/") + 1);
        Logger.i("-----addWavHeader fileName = " + fileName);
        if (TextUtils.isEmpty(fileName)) {
            return false;
        }

        File folder = new File(debugFilePath.substring(0, debugFilePath.lastIndexOf("/")));
        if (!folder.exists() || !folder.isDirectory()) {
            return false;
        }

        Logger.d("addWavHeader folder = " + folder.getPath());
        boolean isConverted = false;
        for (final File file : folder.listFiles()) {
            if (file.isDirectory() || file.getName().contains("wav") || file.getName().contains("txt")) {
                continue;
            }
            if (file.getName().contains(fileName) && !file.getName().contains("speex")) {
                Logger.d("addWavHeader found file = " + file.getPath());
                WavHeaderHelper.addWavHeader(file, !file.getName().contains("USB"));
                isConverted = true;
            }
        }
        return isConverted;
    }

    public static void logResultToFile(Message message) {
        // TODO : write the log at the other thread.
        if (!sIsDebug) {
            return;
        }

        long cid;
        String text;
        if (message instanceof TextMessage) {
            text = ((TextMessage) message).text[0];
            cid = ((TextMessage) message).cid;
        } else if (message instanceof EditTextMessage) {
            text = ((EditTextMessage) message).text[0];
            cid = ((EditTextMessage) message).cid;
        } else if (message instanceof IntermediateMessage) {
            IntermediateMessage iMessage = (IntermediateMessage) message;
            cid = iMessage.cid;
            if (cidToFilePath.indexOfKey(cid) < 0) {
                cidToFilePath.put(cid, sDebugFilepath);
            }
            return;
        } else {
            return;
        }

        String filePath = cidToFilePath.get(cid);
        cidToFilePath.remove(cid);
        Logger.d("logResultToFile filePath = " + filePath);
        if (TextUtils.isEmpty(filePath)) {
            return;
        }

        BufferedWriter bufferedWriter = null;
        try {
            bufferedWriter = new BufferedWriter(new java.io.FileWriter(filePath + ".txt", true));
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (bufferedWriter != null) {
            Logger.d("logResultToFile cid = " + cid + " text = " + text);
            try {
                bufferedWriter.write("cid:" + cid);
                bufferedWriter.newLine();
                bufferedWriter.write("result:" + text);
                bufferedWriter.newLine();
                bufferedWriter.write("-----------------------");
                bufferedWriter.newLine();
                bufferedWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
