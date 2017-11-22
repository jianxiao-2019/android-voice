package com.kikatech.go.dialogflow.sms.reply.stage;

import android.support.annotation.NonNull;

import com.kikatech.go.dialogflow.sms.SmsUtil;
import com.kikatech.go.util.LogUtil;
import com.kikatech.voice.core.dialogflow.scene.ISceneFeedback;
import com.kikatech.voice.core.dialogflow.scene.SceneBase;

/**
 * Created by brad_chang on 2017/11/20.
 */

public class SendMessageReplySmsStage extends BaseReplySmsStage {

    private final String mPhoneNumber;
    private final String mMsgBody;

    SendMessageReplySmsStage(@NonNull SceneBase scene, ISceneFeedback feedback, @NonNull String number, @NonNull String messageBody) {
        super(scene, feedback);
        mPhoneNumber = number;
        mMsgBody = messageBody;
    }

    @Override
    public void action() {
        if (LogUtil.DEBUG)
            LogUtil.log(TAG, "Send Message : \n" + mMsgBody + ", phone:" + mPhoneNumber);
        SmsUtil.sendSms(mSceneBase.getContext(), mPhoneNumber, mMsgBody);
        exitScene();
    }
}