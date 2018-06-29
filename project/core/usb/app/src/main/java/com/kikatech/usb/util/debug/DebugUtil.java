package com.kikatech.usb.util.debug;

/**
 * Created by ryanlin on 12/02/2018.
 */

public class DebugUtil {
    private static final String TAG = "DebugUtil";

    private static String sAudioFilePath;

    public static void setAudioFilePath(String audioFilePath) {
        sAudioFilePath = audioFilePath;
    }

    public static String getAudioFilePath() {
        return sAudioFilePath;
    }
}
