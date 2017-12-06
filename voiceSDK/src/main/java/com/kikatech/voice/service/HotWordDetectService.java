package com.kikatech.voice.service;

import android.content.Context;

import com.kikatech.voice.core.hotword.HotWordVoiceSource;
import com.kikatech.voice.core.hotword.SnowBoyDetector;
import com.kikatech.voice.core.recorder.VoiceRecorder;

import ai.kitt.snowboy.AppResCopy;

/**
 * @author SkeeterWang Created on 2017/12/6.
 */

public class HotWordDetectService {
    private static final String TAG = "HotWordDetectService";

    private VoiceConfiguration mConf;

    private VoiceRecorder mVoiceRecorder;
    private SnowBoyDetector mSnowBoyDetector;

    private HotWordDetectService(VoiceConfiguration conf, SnowBoyDetector.OnHotWordDetectListener listener) {
        mConf = conf;
        mSnowBoyDetector = new SnowBoyDetector(listener);
        mVoiceRecorder = new VoiceRecorder(new HotWordVoiceSource(), mSnowBoyDetector);
    }

    public static HotWordDetectService getService(Context context, VoiceConfiguration conf, SnowBoyDetector.OnHotWordDetectListener listener) {
        AppResCopy.copyResFromAssetsToSD(context);
        return new HotWordDetectService(conf, listener);
    }

    public void start() {
        mSnowBoyDetector.reset();
        mVoiceRecorder.start();
    }

    public void stop() {
        mVoiceRecorder.stop();
    }
}
