package com.kikatech.go.dialogflow.sms.stage;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;

import com.kikatech.go.dialogflow.sms.SendSmsUtil;
import com.kikatech.go.dialogflow.sms.SmsContent;
import com.kikatech.go.util.LogUtil;
import com.kikatech.voice.core.dialogflow.scene.IDialogFlowFeedback;
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
        speak("Send Message !!!", new IDialogFlowFeedback.IToSceneFeedback() {
            @Override
            public void onTtsStart() {
            }

            @Override
            public void onTtsComplete() {
                sendSms();
            }

            @Override
            public void onTtsError() {
                sendSms();
            }

            @Override
            public void onTtsInterrupted() {
                sendSms();
            }
        });
    }

    private void sendSms() {
        if (LogUtil.DEBUG) LogUtil.log(TAG, "Send Message : \n" + getSmsContent().toString());
        SmsContent sc = getSmsContent();
        SendSmsUtil.sensSms(mSceneBase.getContext(), sc.getChoosedPhoneNumber(), sc.getSmsBody());
        exitScene();
    }
}
