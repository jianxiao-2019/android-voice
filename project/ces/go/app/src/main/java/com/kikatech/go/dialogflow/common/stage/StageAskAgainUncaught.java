package com.kikatech.go.dialogflow.common.stage;

import android.os.Bundle;
import android.support.annotation.NonNull;

import com.kikatech.go.dialogflow.SceneUtil;
import com.kikatech.go.dialogflow.model.TtsText;
import com.kikatech.voice.core.dialogflow.intent.Intent;
import com.kikatech.voice.core.dialogflow.scene.ISceneFeedback;
import com.kikatech.voice.core.dialogflow.scene.SceneBase;
import com.kikatech.voice.core.dialogflow.scene.SceneStage;

/**
 * @author SkeeterWang Created on 2017/12/13.
 */

public class StageAskAgainUncaught extends SceneStage {

    public StageAskAgainUncaught(@NonNull SceneBase scene, ISceneFeedback feedback) {
        super(scene, feedback);
    }

    @Override
    public SceneStage next(String action, Bundle extra) {
        switch (action) {
            case Intent.ACTION_UNKNOWN:
            case Intent.ACTION_UNCAUGHT:
                return new StageStopSession(mSceneBase, mFeedback);
        }
        return null;
    }

    @Override
    protected void prepare() {
    }

    @Override
    public void doAction() {
        onStageActionStart();
        action();
    }

    @Override
    protected void action() {
        final String PRE_UNCAUGHT = "Please say again";
        String uiText = PRE_UNCAUGHT;
        String ttsText = SceneUtil.getResponseNotGet(mSceneBase.getContext(), PRE_UNCAUGHT);
        TtsText tText = new TtsText(SceneUtil.ICON_COMMON, uiText);
        Bundle args = new Bundle();
        args.putParcelable(SceneUtil.EXTRA_TTS_TEXT, tText);
        speak(ttsText, args);
    }
}
