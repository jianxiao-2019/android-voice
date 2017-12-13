package com.kikatech.go.dialogflow;

import android.os.Bundle;
import android.support.annotation.NonNull;

import com.kikatech.go.util.LogUtil;
import com.kikatech.voice.core.dialogflow.scene.ISceneFeedback;
import com.kikatech.voice.core.dialogflow.scene.SceneBase;
import com.kikatech.voice.core.dialogflow.scene.SceneStage;

/**
 * @author SkeeterWang Created on 2017/12/12.
 */

public abstract class BaseSceneStage extends SceneStage {

    public BaseSceneStage(@NonNull SceneBase scene, ISceneFeedback feedback) {
        super(scene, feedback);
        if (LogUtil.DEBUG) LogUtil.log(TAG, "init, AsrMode : " + AsrConfigUtil.getAsrModeName(getAsrMode()));
        updateAsrConfig(getAsrMode());
    }

    @Override
    protected void speak(String text, Bundle extras) {
        if (isUncaughtLoop) {
            String wrappedTts = SceneUtil.getResponseNotGet(mSceneBase.getContext(), text);
            super.speak(wrappedTts, extras);
        } else {
            super.speak(text, extras);
        }
    }

    void updateAsrConfig(@AsrConfigUtil.ASRMode int mode) {
        AsrConfigUtil.getConfig(mAsrConfig, mode);
    }

    protected @AsrConfigUtil.ASRMode int getAsrMode() {
        return AsrConfigUtil.SUGGEST_ASR_MODE_DEFAULT;
    }
}
