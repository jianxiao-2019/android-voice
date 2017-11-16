package com.kikatech.go.dialogflow.sms.stage;

import android.os.Bundle;
import android.support.annotation.NonNull;

import com.kikatech.go.dialogflow.sms.SmsContent;
import com.kikatech.voice.core.dialogflow.scene.ISceneFeedback;
import com.kikatech.voice.core.dialogflow.scene.SceneBase;
import com.kikatech.voice.core.dialogflow.scene.SceneStage;

/**
 * Created by brad_chang on 2017/11/15.
 */

public class StageAskForChooseNumbers extends BaseSendSmsStage {

    // SendSMS 2.6
    StageAskForChooseNumbers(@NonNull SceneBase scene, ISceneFeedback feedback) {
        super(scene, feedback);
    }

    @Override
    public SceneStage next(String action, Bundle extra) {
        SmsContent sc = parseSmsContent(extra);
        if (sc.isSmsBodyAvailable()) {
            // SendSMS 2.9
            return new StageAskForSendSmsToContact(mSceneBase, mFeedback);
        } else {
            // SendSMS 2.8
            return new StageAskForSmsBody(mSceneBase, mFeedback);
        }
    }

    @Override
    public void action() {
        speak("Choose a number from following list");
    }
}
