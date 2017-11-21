package com.kikatech.go.dialogflow.sms.reply.stage;

import android.os.Bundle;
import android.support.annotation.NonNull;

import com.kikatech.go.dialogflow.sms.SmsUtil;
import com.kikatech.go.dialogflow.sms.reply.SceneActions;
import com.kikatech.go.message.sms.SmsObject;
import com.kikatech.go.util.LogUtil;
import com.kikatech.voice.core.dialogflow.scene.ISceneFeedback;
import com.kikatech.voice.core.dialogflow.scene.SceneBase;
import com.kikatech.voice.core.dialogflow.scene.SceneStage;

/**
 * Created by brad_chang on 2017/11/20.
 */

public class AskMsgBodyReplySmsStage extends BaseReplySmsStage {

    private final SmsObject mSmsObject;

    AskMsgBodyReplySmsStage(@NonNull SceneBase scene, ISceneFeedback feedback, @NonNull SmsObject sms) {
        super(scene, feedback);
        mSmsObject = sms;
    }

    @Override
    public SceneStage getNextStage(String action, Bundle extra) {
        if(action.equals(SceneActions.ACTION_REPLY_SMS_MSG_BODY)) {
            String messageBody = SmsUtil.parseTagAny(extra);
            return new ConfirmMsgBodyReplySmsStage(mSceneBase, mFeedback, mSmsObject, messageBody);
        }
        return null;
    }

    @Override
    public void action() {
        String msg = "Please say your message."; // doc 20
        if (LogUtil.DEBUG) LogUtil.log(TAG, msg);
        speak(msg);
    }
}
