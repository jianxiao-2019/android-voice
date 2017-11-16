package com.kikatech.go.dialogflow.sms;

import android.content.Context;
import android.os.Bundle;

import com.kikatech.go.dialogflow.sms.stage.StageSendSmsIdle;
import com.kikatech.voice.core.dialogflow.scene.ISceneFeedback;
import com.kikatech.voice.core.dialogflow.scene.SceneBase;
import com.kikatech.voice.core.dialogflow.scene.SceneStage;

/**
 * Created by brad_chang on 2017/11/15.
 */

public class SceneSendSms extends SceneBase {

    public static final String SCENE = "SendSMS";

    private SmsContent smsContent;

    SceneSendSms(Context context, ISceneFeedback feedback) {
        super(context, feedback);
    }

    public SmsContent getSmsContent() {
        return smsContent;
    }

    public void setSmsContent(SmsContent sc) {
        smsContent = sc;
    }

    @Override
    protected String scene() {
        return SCENE;
    }

    @Override
    protected void onExit() {

    }

    @Override
    protected SceneStage idle() {
        return new StageSendSmsIdle(this, mFeedback);
    }
}
