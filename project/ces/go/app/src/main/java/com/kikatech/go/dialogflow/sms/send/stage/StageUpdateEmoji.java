package com.kikatech.go.dialogflow.sms.send.stage;

import android.os.Bundle;
import android.support.annotation.NonNull;

import com.kikatech.go.dialogflow.sms.send.SceneActions;
import com.kikatech.go.util.LogUtil;
import com.kikatech.voice.core.dialogflow.intent.Intent;
import com.kikatech.voice.core.dialogflow.scene.ISceneFeedback;
import com.kikatech.voice.core.dialogflow.scene.SceneBase;
import com.kikatech.voice.core.dialogflow.scene.SceneStage;

/**
 * Created by brad_chang on 2017/11/16.
 */

public class StageUpdateEmoji extends BaseSendSmsStage {

    // SendSMS 2.10
    StageUpdateEmoji(@NonNull SceneBase scene, ISceneFeedback feedback) {
        super(scene, feedback);
    }

    @Override
    protected SceneStage getNextStage(String action, Bundle extra) {
        switch (action) {
            case SceneActions.ACTION_SEND_SMS_YES:
                return new StageAskAddEmoji(mSceneBase, mFeedback);
            case SceneActions.ACTION_SEND_SMS_NO:
            case SceneActions.ACTION_SEND_SMS_CHANGE_SMS_BODY:
                return new StageAskForSmsBody(mSceneBase, mFeedback);
            case SceneActions.ACTION_SEND_SMS_MSGBODY:
                return new StageAskForSendSmsToContact(mSceneBase, mFeedback);
            default:
                if (LogUtil.DEBUG) LogUtil.log(TAG, "Unsupported action:" + action);
                break;
        }
        return this;
    }

    @Override
    public void doAction() {
    }

    @Override
    public void action() {
        if (LogUtil.DEBUG) {
            LogUtil.log(TAG, "Emoji, updated : " + getSmsContent().getEmojiUnicode() + " <" + getSmsContent().getEmojiDesc() + ">");
        }
    }
}