package com.kikatech.go.dialogflow.sms.stage;

import android.os.Bundle;
import android.support.annotation.NonNull;

import com.kikatech.go.util.LogUtil;
import com.kikatech.voice.core.dialogflow.scene.ISceneFeedback;
import com.kikatech.voice.core.dialogflow.scene.SceneBase;
import com.kikatech.voice.core.dialogflow.scene.SceneStage;

/**
 * Created by brad_chang on 2017/11/16.
 */

public class StageSendSmsAskEmoji extends BaseSendSmsStage {

    // SendSMS 2.10
    StageSendSmsAskEmoji(@NonNull SceneBase scene, ISceneFeedback feedback) {
        super(scene, feedback);
    }

    @Override
    public SceneStage next(String action, Bundle extra) {
        if(action.equals("send.emoji.confirm")) {
            // Send sms with Emoji
        } else if(action.equals("send.emoji.confirm.no")) {
            // Send sms without Emoji
        } else if(action.equals("send.emoji.confirm.add.others")) {

        }
        return null;
    }

    @Override
    public void action() {
        if(LogUtil.DEBUG) {
            LogUtil.log("BaseSendSmsStage", "action : do nothing");
        }
    }
}
