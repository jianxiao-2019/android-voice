package com.kikatech.go.dialogflow.im.send.stage;

import android.os.Bundle;
import android.support.annotation.NonNull;

import com.kikatech.go.dialogflow.im.IMContent;
import com.kikatech.go.dialogflow.im.send.SceneActions;
import com.kikatech.voice.core.dialogflow.scene.ISceneFeedback;
import com.kikatech.voice.core.dialogflow.scene.SceneBase;
import com.kikatech.voice.core.dialogflow.scene.SceneStage;

/**
 * Created by brad_chang on 2017/11/23.
 */

public class StageConfirmSendTarget extends BaseSendIMStage {
    StageConfirmSendTarget(@NonNull SceneBase scene, ISceneFeedback feedback) {
        super(scene, feedback);
    }

    @Override
    protected SceneStage getNextStage(String action, Bundle extra) {
        IMContent imc = getIMContent();
        // Check yes or no
        switch (action) {
            case SceneActions.ACTION_SEND_IM_YES:
                imc.userConfirmSendTarget();
                return getCheckIMBodyStage(TAG, imc, mSceneBase, mFeedback);
            case SceneActions.ACTION_SEND_IM_NO:
                return getCheckSendTargetStage(TAG, imc, mSceneBase, mFeedback);
            default:
                // TODO
                return getCheckSendTargetStage(TAG, imc, mSceneBase, mFeedback);
        }
    }

    @Override
    public void action() {
        IMContent imc = getIMContent();
        speak("Do you mean " + imc.getSendTarget() + " ?");
    }
}