package com.kikatech.go.dialogflow.sms.send.stage;

import android.os.Bundle;
import android.support.annotation.NonNull;

import com.kikatech.voice.core.dialogflow.scene.ISceneFeedback;
import com.kikatech.voice.core.dialogflow.scene.SceneBase;
import com.kikatech.voice.core.dialogflow.scene.SceneStage;

/**
 * Created by brad_chang on 2017/11/15.
 */

public class StageAskSendTargetCorrect extends BaseSendSmsStage {

    /**
     * SendSMS 2.4 確認傳訊對象
     */
    StageAskSendTargetCorrect(@NonNull SceneBase scene, ISceneFeedback feedback) {
        super(scene, feedback);
    }

    @Override
    protected SceneStage getNextStage(String action, Bundle extra) {
        return getStageCheckNumberCount(TAG, getSmsContent(), mSceneBase, mFeedback);
    }

    @Override
    public void action() {
        speak("Is " + getSmsContent().getMatchedName() + " correct ?");
    }
}
