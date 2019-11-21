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
import com.kikatech.voice.util.log.LogUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by ryanlin on 12/02/2018.
 */

public class DebugUtil {

    private static final String TAG = "DebugUtil";

    private static final String PRE_PHONE = "Phone_";
    private static final String PRE_KIKA_GO = "Kikago_";
    private static final String PRE_LOCAL_BTB0 = "Local_btb0_";
    private static final String PRE_UNKNOWN = "Unknown_";
    private static final String PRE_LOCAL_PM0 = "Local_pm0_";
    private static final String PRE_LOCAL_PM180 = "Local_pm180_";
    private static final String PRE_LOCAL_BTB180 = "Local_btb180_";
    private static final String PRE_LOCAL_MS = "Local_ms_";


    private static boolean sIsDebug = false;

    private static String sAsrAudioFilePath;

    private static String status;

    private static LongSparseArray<String> cidToFilePath = new LongSparseArray<>();

    public static void updateCacheDir(VoiceConfiguration conf) {
        sIsDebug = conf.getIsDebugMode();

        if (!sIsDebug) {
            return;
        }
        cidToFilePath.clear();
    }

    public static void getStatus(String str) {
        status = str;
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
            if (status.contains("VL630-PM-0")){
                return PRE_LOCAL_PM0;
            } else if (status.contains("VL630-PM-180") || status.contains("VL632-PM-180")){
                return PRE_LOCAL_PM180;
            } else if (status.contains("VL630-BTB-0") || status.contains("VL632-BTB-0")) {
                return PRE_LOCAL_BTB0;
            } else if (status.contains("VL630-BTB-180") || status.contains("VL632-BTB-180")) {
                return PRE_LOCAL_BTB180;
            } else if (status.contains("VL630-MS-MS") || status.contains("VL632-MS-MS")) {
                return PRE_LOCAL_MS;
            }
            return PRE_UNKNOWN;
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
                LogUtils.d(TAG,"convertCurrentPcmToWav result = " + result);
            }
        }).start();
        sAsrAudioFilePath = null;
    }

    private static boolean addWavHeader(String debugFilePath) {
        LogUtils.i(TAG,"-----addWavHeader mDebugFileName = " + debugFilePath);
        if (TextUtils.isEmpty(debugFilePath)) {
            return false;
        }
        String fileName = debugFilePath.substring(debugFilePath.lastIndexOf("/") + 1);
        LogUtils.i(TAG,"-----addWavHeader fileName = " + fileName);
        if (TextUtils.isEmpty(fileName)) {
            return false;
        }

        File folder = new File(debugFilePath.substring(0, debugFilePath.lastIndexOf("/")));
        if (!folder.exists() || !folder.isDirectory()) {
            return false;
        }

        LogUtils.d(TAG,"addWavHeader folder = " + folder.getPath());
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
                LogUtils.d(TAG,"addWavHeader found file = " + file.getPath());
                WavHeaderHelper.addWavHeader(file, !file.getName().contains("USB"));
                isConverted = true;
            }
        }
        return isConverted;
    }

    private static long offsetInitialCid = 0;
    private static int cidIndex = 0;

    public static void setStarCid(long cid) {
        offsetInitialCid = cid;
        cidIndex = 0;
    }

    public static void logResultToFile(Message message) {
        // TODO : write the log at the other thread.
        if (!sIsDebug) {
            return;
        }

        long cid;
        long endCid = 0;
        String text;
        if (message instanceof TextMessage) {
            text = ((TextMessage) message).text[0];
            cid = ((TextMessage) message).cid;
            endCid = ((TextMessage) message).endCid;
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
        LogUtils.d(TAG,"logResultToFile filePath = " + filePath);
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
            LogUtils.d(TAG,"logResultToFile cid = " + cid + " text = " + text);
            try {
                cidIndex += 1;
                if (cid != 0 && endCid != 0) {
                    bufferedWriter.write(String.valueOf(cidIndex));
                    bufferedWriter.newLine();
                    bufferedWriter.write(convertCidToSrt(cid));
                    bufferedWriter.write(" --> ");
                    bufferedWriter.write(convertCidToSrt(endCid));
                } else {
                    bufferedWriter.write(convertCidToDate(cid));
                }
                bufferedWriter.newLine();
                bufferedWriter.write(text);
                bufferedWriter.newLine();
                bufferedWriter.newLine();
                bufferedWriter.newLine();
                bufferedWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static String convertCidToDate(long cid) {
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd,yyyy HH:mm:ss");
        Date resultdate = new Date(cid);
        return sdf.format(resultdate);
    }

    private static String convertCidToSrt(long cid) {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss,SSS");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        Date resultdate = new Date(cid - offsetInitialCid);
        return sdf.format(resultdate);
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
            LogUtils.d(TAG,"logTextToFile = " + title + ": " + text);
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
