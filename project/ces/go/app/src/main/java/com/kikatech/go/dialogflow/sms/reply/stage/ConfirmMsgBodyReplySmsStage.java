package com.kikatech.go.dialogflow.sms.reply.stage;

import android.os.Bundle;
import android.support.annotation.NonNull;

import com.kikatech.go.dialogflow.sms.reply.SceneActions;
import com.kikatech.go.message.sms.SmsObject;
import com.kikatech.go.util.LogUtil;
import com.kikatech.voice.core.dialogflow.scene.ISceneFeedback;
import com.kikatech.voice.core.dialogflow.scene.SceneBase;
import com.kikatech.voice.core.dialogflow.scene.SceneStage;

/**
 * Created by brad_chang on 2017/11/20.
 */

public class ConfirmMsgBodyReplySmsStage extends BaseReplySmsStage {

    private final String mMsgBody;
    private final SmsObject mSmsObject;

    ConfirmMsgBodyReplySmsStage(@NonNull SceneBase scene, ISceneFeedback feedback, @NonNull SmsObject sms, String messageBody) {
        super(scene, feedback);
        mSmsObject = sms;
        mMsgBody = messageBody;
    }

    @Override
    public SceneStage getNextStage(String action, Bundle extra) {
        switch (action) {
            case SceneActions.ACTION_REPLY_SMS_YES:
                return new SendMessageReplySmsStage(mSceneBase, mFeedback, mSmsObject.getId(), mMsgBody);
            case SceneActions.ACTION_REPLY_SMS_CHANGE:
                return new AskMsgBodyReplySmsStage(mSceneBase, mFeedback, mSmsObject);
            default:
                if (LogUtil.DEBUG) LogUtil.log(TAG, "Unsupported command : " + action + ", ask again");
                return new ConfirmMsgBodyReplySmsStage(mSceneBase, mFeedback, mSmsObject, mMsgBody);
        }
    }

    @Override
    public void action() {
        String msg = "Send message \"" + mMsgBody + "\" to " + mSmsObject.getUserName() + ". Send it or change it ?";
        if (LogUtil.DEBUG) LogUtil.log(TAG, msg);
        speak(msg);
    }
}