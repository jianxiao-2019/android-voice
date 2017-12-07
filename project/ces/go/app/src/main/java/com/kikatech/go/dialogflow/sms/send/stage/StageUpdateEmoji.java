package com.kikatech.go.dialogflow.sms.send.stage;

import android.os.Bundle;
import android.support.annotation.NonNull;

import com.kikatech.go.dialogflow.sms.SmsContent;
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
        SmsContent sc = getSmsContent();
        switch (action) {
            case SceneActions.ACTION_SEND_SMS_YES:
                if(sc.hasEmoji()) {
                    return new StageAskAddEmoji(mSceneBase, mFeedback);
                } else {
                    return new StageSendSmsConfirm(mSceneBase, mFeedback);
                }
            case SceneActions.ACTION_SEND_SMS_NO:
                return new StageAskForSmsBody(mSceneBase, mFeedback);
            default:
                if (LogUtil.DEBUG) LogUtil.log(TAG, "Unsupported action:" + action);
                return new StageAskForSmsBody(mSceneBase, mFeedback);
        }
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