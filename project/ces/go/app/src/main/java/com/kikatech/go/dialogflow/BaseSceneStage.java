package com.kikatech.go.dialogflow;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Pair;

import com.kikatech.go.util.LogUtil;
import com.kikatech.voice.core.dialogflow.scene.ISceneFeedback;
import com.kikatech.voice.core.dialogflow.scene.SceneBase;
import com.kikatech.voice.core.dialogflow.scene.SceneStage;
import com.kikatech.voice.core.tts.TtsSource;
import com.kikatech.voice.service.conf.AsrConfiguration;

/**
 * @author SkeeterWang Created on 2017/12/12.
 */

public abstract class BaseSceneStage extends SceneStage {

    public BaseSceneStage(@NonNull SceneBase scene, ISceneFeedback feedback) {
        super(scene, feedback);
        if (LogUtil.DEBUG) {
            LogUtil.log(TAG, "init, AsrMode : " + AsrConfigUtil.getAsrModeName(getAsrMode()));
        }
        updateAsrLocale(getAsrLocale());
        updateAsrConfig(getAsrMode());
    }

    protected void updateAsrConfig(@AsrConfigUtil.ASRMode int mode) {
        AsrConfigUtil.getConfig(mAsrConfig, mode);
    }

    protected void updateAsrLocale(String locale) {
        mAsrConfig.setLocale(locale);
    }

    @AsrConfigUtil.ASRMode
    protected int getAsrMode() {
        return AsrConfigUtil.ASRMode.ASR_MODE_DEFAULT;
    }

    protected String getAsrLocale() {
        return AsrConfiguration.SupportedLanguage.EN_US;
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
}
