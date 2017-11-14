package com.kikatech.go.dialogflow.telephony.outgoing.stage;

import android.os.Bundle;
import android.support.annotation.NonNull;

import com.kikatech.go.util.LogUtil;
import com.kikatech.voice.core.dialogflow.scene.ISceneFeedback;
import com.kikatech.voice.core.dialogflow.scene.SceneBase;
import com.kikatech.voice.core.dialogflow.scene.SceneStage;

/**
 * @author SkeeterWang Created on 2017/11/13.
 */

public class StageCancel extends SceneStage {
    private static final String TAG = "StageCancel";

    public StageCancel(@NonNull SceneBase scene, ISceneFeedback feedback) {
        super(scene, feedback);
    }

    @Override
    public SceneStage next(String action, Bundle extra) {
        return null;
    }

    @Override
    public void action() {
        String speech = "ok, canceled it.";
        if (LogUtil.DEBUG) {
            LogUtil.logv(TAG, speech);
        }
        speak(speech);
        exitScene();
    }
}
