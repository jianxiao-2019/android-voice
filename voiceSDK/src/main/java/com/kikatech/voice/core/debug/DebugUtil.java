package com.kikatech.voice.core.debug;

import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.LongSparseArray;

import com.kikatech.voice.core.recorder.IVoiceSource;
import com.kikatech.voice.core.webservice.message.AlterMessage;
import com.kikatech.voice.core.webservice.message.IntermediateMessage;
import com.kikatech.voice.core.webservice.message.Message;
import com.kikatech.voice.core.webservice.message.TextMessage;
import com.kikatech.voice.service.conf.VoiceConfiguration;
import com.kikatech.voice.util.log.Logger;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;

/**
 * Created by ryanlin on 12/02/2018.
 */

public class DebugUtil {

    private static final String PRE_PHONE = "Phone_";
    private static final String PRE_KIKA_GO = "Kikago_";
    private static final String PRE_LOCAL = "Local_";
    private static final String PRE_UNKNOWN = "Unknown_";

    private static boolean sIsDebug = false;

    private static String sAsrAudioFilePath;

    private static LongSparseArray<String> cidToFilePath = new LongSparseArray<>();

    public static void updateCacheDir(VoiceConfiguration conf) {
        sIsDebug = conf.getIsDebugMode();
        Logger.updateDebugState(sIsDebug);
        if (!sIsDebug) {
            return;
        }
        cidToFilePath.clear();
    }

    public static boolean isDebug() {
        return sIsDebug;
    }

    public static void setAsrAudioPath(String asrAudioPath) {
        sAsrAudioFilePath = asrAudioPath;
    }

    public static String getAsrAudioFilePath() {
        return sAsrAudioFilePath;
    }

    public static String getFilePrefix(VoiceConfiguration conf) {
        IVoiceSource source = conf.getVoiceSource();
        if (source == null) {
            return PRE_PHONE;
        } else if (source.getClass().getSimpleName().contains("Kika")) {
            return PRE_KIKA_GO;
        } else if (source.getClass().getSimpleName().contains("Local")) {
            return PRE_LOCAL;
        }
        return PRE_UNKNOWN;
    }


    public static void convertCurrentPcmToWav() {
        if (TextUtils.isEmpty(sAsrAudioFilePath)) {
            return;
        }

        final String filePath = sAsrAudioFilePath;
        new Thread(new Runnable() {
            @Override
            public void run() {
                boolean result = addWavHeader(filePath);
                Logger.d("convertCurrentPcmToWav result = " + result);
            }
        }).start();
        sAsrAudioFilePath = null;
    }

    private static boolean addWavHeader(String debugFilePath) {
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
        File[] files = folder.listFiles();
        if (files == null) {
            return false;
        }

        boolean isConverted = false;
        for (final File file : files) {
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
        } else if (message instanceof AlterMessage) {
            text = ((AlterMessage) message).text[0];
            cid = ((AlterMessage) message).cid;
        } else if (message instanceof IntermediateMessage) {
            IntermediateMessage iMessage = (IntermediateMessage) message;
            cid = iMessage.cid;
            if (cidToFilePath.indexOfKey(cid) < 0) {
                cidToFilePath.put(cid, sAsrAudioFilePath);
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

    public static void logTextToFile(@NonNull String title, @NonNull String text) {
        if (TextUtils.isEmpty(sAsrAudioFilePath)) {
            return;
        }

        String filePath = sAsrAudioFilePath;
        BufferedWriter bufferedWriter = null;
        try {
            bufferedWriter = new BufferedWriter(new java.io.FileWriter(filePath + ".txt", true));
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (bufferedWriter != null) {
            Logger.d("logTextToFile = " + title + ": " + text);
            try {
                bufferedWriter.write(title + ": " + text);
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
