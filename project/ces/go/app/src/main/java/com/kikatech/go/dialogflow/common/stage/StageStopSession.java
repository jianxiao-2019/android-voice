package com.kikatech.go.dialogflow.common.stage;

import android.os.Bundle;
import android.support.annotation.NonNull;

import com.kikatech.go.dialogflow.SceneUtil;
import com.kikatech.voice.core.dialogflow.scene.ISceneFeedback;
import com.kikatech.voice.core.dialogflow.scene.SceneBase;
import com.kikatech.voice.core.dialogflow.scene.SceneStage;

/**
 * @author SkeeterWang Created on 2017/11/29.
 */

public class StageStopSession extends SceneStage {

    public StageStopSession(@NonNull SceneBase scene, ISceneFeedback feedback) {
        super(scene, feedback);
    }

    @Override
    public SceneStage next(String action, Bundle extra) {
        return null;
    }

    @Override
    protected void prepare() {
    }

    @Override
    public void doAction() {
        action();
    }

    @Override
    protected void action() {
        final String PRE_UNKNOWN = "Canceling conversation";
        String uiText = PRE_UNKNOWN;
        String ttsText = SceneUtil.getIntentUnknown(mSceneBase.getContext(), PRE_UNKNOWN);
        Bundle args = new Bundle();
        args.putString(SceneUtil.EXTRA_UI_TEXT, uiText);
        speak(ttsText, args);
    }

    @Override
    public void onStageActionDone(boolean isInterrupted) {
        exitScene();
    }
}
