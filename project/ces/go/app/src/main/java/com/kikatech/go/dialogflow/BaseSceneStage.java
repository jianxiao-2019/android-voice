package com.kikatech.go.dialogflow;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Pair;

import com.kikatech.go.util.LogUtil;
import com.kikatech.go.util.timer.CountingTimer;
import com.kikatech.voice.core.dialogflow.scene.ISceneFeedback;
import com.kikatech.voice.core.dialogflow.scene.SceneBase;
import com.kikatech.voice.core.dialogflow.scene.SceneStage;
import com.kikatech.voice.core.tts.TtsSource;

/**
 * @author SkeeterWang Created on 2017/12/12.
 */

public abstract class BaseSceneStage extends SceneStage {

    private static final long DEFAULT_STAGE_TIMEOUT = 5000;

    private CountingTimer mStageTimeoutTimer;


    public BaseSceneStage(@NonNull SceneBase scene, ISceneFeedback feedback) {
        super(scene, feedback);
        if (LogUtil.DEBUG) {
            LogUtil.log(TAG, "init, AsrMode : " + AsrConfigUtil.getAsrModeName(getAsrMode()));
        }
        updateAsrConfig(getAsrMode());
    }

    private void updateAsrConfig(@AsrConfigUtil.ASRMode int mode) {
        AsrConfigUtil.getConfig(mAsrConfig, mode);
    }

    @AsrConfigUtil.ASRMode
    protected int getAsrMode() {
        return AsrConfigUtil.SUGGEST_ASR_MODE_DEFAULT;
    }


    @Override
    protected void speak(String text, Bundle extras) {
        if (isUncaughtLoop) {
            String wrappedTts = SceneUtil.getResponseNotGet(mSceneBase.getContext());

            Pair<String, Integer>[] pairs = new Pair[2];
            pairs[0] = new Pair<>(wrappedTts, TtsSource.TTS_SPEAKER_1);
            pairs[1] = new Pair<>(text, TtsSource.TTS_SPEAKER_1);

            super.speak(pairs, extras);
        } else {
            super.speak(text, extras);
        }
    }


    protected void startTimeoutTimer(CountingTimer.ICountingListener listener) {
        startTimeoutTimer(DEFAULT_STAGE_TIMEOUT, listener);
    }

    protected void startTimeoutTimer(long millis, CountingTimer.ICountingListener listener) {
        stopTimeoutTimer();
        mStageTimeoutTimer = new CountingTimer(millis, listener);
        mStageTimeoutTimer.start();
    }

    protected void stopTimeoutTimer() {
        if (mStageTimeoutTimer != null && mStageTimeoutTimer.isCounting()) {
            mStageTimeoutTimer.stop();
        }
    }
}
