package com.kikatech.go.dialogflow.telephony.incoming.stage;

import android.os.Bundle;

import com.kikatech.go.telephony.TelephonyServiceManager;
import com.kikatech.voice.core.dialogflow.scene.ISceneFeedback;
import com.kikatech.voice.core.dialogflow.scene.SceneBase;
import com.kikatech.voice.core.dialogflow.scene.SceneStage;

/**
 * Created by tianli on 17-11-11.
 */

public class StageAnswer extends SceneStage {

    public StageAnswer(SceneBase scene, ISceneFeedback feedback) {
        super(scene, feedback);
    }

    @Override
    public SceneStage next(String action, Bundle extra) {
        return null;
    }

    @Override
    public void action() {
        String toast = "Ok, answered this call.";
        speak(toast);
        // TODO: 17-11-13 是否等tts结束之后再接电话
        answerPhoneCall();
        exitScene();
    }

    private void answerPhoneCall() {
//        if (LogUtil.DEBUG) {
//            LogUtil.log(TAG, "answerPhoneCall");
//        }
        TelephonyServiceManager.getIns().answerPhoneCall(mSceneBase.getContext());
        TelephonyServiceManager.getIns().turnOnSpeaker(mSceneBase.getContext());
    }
}
