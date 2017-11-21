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

public class StageReject extends SceneStage {

    public StageReject(SceneBase scene, ISceneFeedback feedback) {
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
        rejectPhoneCall();
    }

    private void rejectPhoneCall() {
        TelephonyServiceManager.getIns().killPhoneCall(mSceneBase.getContext());
        exitScene();
    }

}
