package com.kikatech.go.dialogflow.telephony.incoming.stage;

import android.os.Bundle;

import com.kikatech.go.telephony.TelephonyServiceManager;
import com.kikatech.go.util.LogUtil;
import com.kikatech.voice.core.dialogflow.scene.IDialogFlowFeedback;
import com.kikatech.voice.core.dialogflow.scene.ISceneFeedback;
import com.kikatech.voice.core.dialogflow.scene.SceneBase;
import com.kikatech.voice.core.dialogflow.scene.SceneStage;

/**
 * Created by tianli on 17-11-11.
 */

public class StageAnswer extends SceneStage {
    private static final String TAG = "StageAnswer";

    public StageAnswer(SceneBase scene, ISceneFeedback feedback) {
        super(scene, feedback);
    }

    @Override
    public SceneStage next(String action, Bundle extra) {
        return null;
    }

    @Override
    public void prepare() {
    }

    @Override
    public void action() {
        answerPhoneCall();
    }

    private void answerPhoneCall() {
        TelephonyServiceManager.getIns().answerPhoneCall(mSceneBase.getContext());
        TelephonyServiceManager.getIns().turnOnSpeaker(mSceneBase.getContext());
        exitScene();
    }
}
