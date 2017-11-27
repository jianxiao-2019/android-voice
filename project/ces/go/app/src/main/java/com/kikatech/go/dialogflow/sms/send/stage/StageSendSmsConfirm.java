package com.kikatech.go.dialogflow.sms.send.stage;

import android.os.Bundle;
import android.support.annotation.NonNull;

import com.kikatech.go.dialogflow.sms.SmsContent;
import com.kikatech.go.dialogflow.sms.SmsUtil;
import com.kikatech.go.util.LogUtil;
import com.kikatech.voice.core.dialogflow.scene.ISceneFeedback;
import com.kikatech.voice.core.dialogflow.scene.SceneBase;
import com.kikatech.voice.core.dialogflow.scene.SceneStage;

/**
 * Created by brad_chang on 2017/11/16.
 */

public class StageSendSmsConfirm extends BaseSendSmsStage {

    StageSendSmsConfirm(@NonNull SceneBase scene, ISceneFeedback feedback) {
        super(scene, feedback);
    }

    @Override
    public SceneStage next(String action, Bundle extra) {
        return null;
    }

    @Override
    public void action() {
        sendSms();
    }

    private void sendSms() {
        if (LogUtil.DEBUG) LogUtil.log(TAG, "Send Message : \n" + getSmsContent().toString());
        SmsContent sc = getSmsContent();
        SmsUtil.sendSms(mSceneBase.getContext(), sc.getChosenPhoneNumber(), sc.getSmsBody());
        exitScene();
    }
}