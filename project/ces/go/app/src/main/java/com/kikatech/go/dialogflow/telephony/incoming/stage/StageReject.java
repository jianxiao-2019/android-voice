package com.kikatech.go.dialogflow.telephony.incoming.stage;

import android.os.Bundle;

import com.kikatech.go.telephony.TelephonyServiceManager;
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
    public void action() {
        speak("Ok, rejected this call.");
        rejectPhoneCall();
        exitScene();
    }

    private void rejectPhoneCall() {
        TelephonyServiceManager.getIns().killPhoneCall(mSceneBase.getContext());
    }

}