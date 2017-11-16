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

public class StageAskForSendSmsToContact extends BaseSendSmsStage {

    // SendSMS 2.9
    StageAskForSendSmsToContact(@NonNull SceneBase scene, ISceneFeedback feedback) {
        super(scene, feedback);
    }

    @Override
    public SceneStage next(String action, Bundle extra) {
        if(action.equals("confirm")) {
            return new StageSendSmsConfirm(mSceneBase, mFeedback);
        } else if(action.equals("change")) {
            return new StageSendSmsChange(mSceneBase, mFeedback);
        }

        // StageSendSmsAskEmoji
        return null;
    }

    @Override
    public void action() {
        SmsContent sc = getSmsContent();
        speak("Send text " + sc.getSmsBody() + " to " + sc.getContact() + " ?");
    }
}
