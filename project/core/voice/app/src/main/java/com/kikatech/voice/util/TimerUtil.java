package com.kikatech.voice.util;

import android.os.SystemClock;

import com.kikatech.voice.util.log.Logger;

/**
 * Created by ryanlin on 25/09/2017.
 */

public class TimerUtil {

    public static long sUserVoiceEndTime = -1;
    public static long sReceiveFirstCommandTime = -1;
    public static long sReceiveFinalCommandTime = -1;
    public static long sPrepareTtsTime = -1;
    public static long sReadyToPlayTtsTime = -1;

    private static long sBeginTime = -1;

    public static void startTag() {
        sBeginTime = SystemClock.elapsedRealtime();
    }

    public static void logTag(String tag) {
        if (sBeginTime == -1) {
            Logger.e("logTag error : sBeginTime == -1");
            return;
        }
        Logger.w("[" + tag + "] duration = " + (SystemClock.elapsedRealtime() - sBeginTime));
    }
}
