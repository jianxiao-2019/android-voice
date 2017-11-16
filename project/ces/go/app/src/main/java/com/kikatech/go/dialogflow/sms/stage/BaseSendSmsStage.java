package com.kikatech.go.dialogflow.sms.stage;

import android.os.Bundle;
import android.support.annotation.NonNull;

import com.kikatech.go.dialogflow.sms.SceneSendSms;
import com.kikatech.go.dialogflow.sms.SendSmsUtil;
import com.kikatech.go.dialogflow.sms.SmsContent;
import com.kikatech.go.util.LogUtil;
import com.kikatech.voice.core.dialogflow.scene.ISceneFeedback;
import com.kikatech.voice.core.dialogflow.scene.SceneBase;
import com.kikatech.voice.core.dialogflow.scene.SceneStage;

/**
 * Created by brad_chang on 2017/11/16.
 */

public class BaseSendSmsStage extends SceneStage {

    protected final String TAG = getClass().getSimpleName();

    BaseSendSmsStage(@NonNull SceneBase scene, ISceneFeedback feedback) {
        super(scene, feedback);
        if (LogUtil.DEBUG) LogUtil.log(TAG, "init");
    }

    SmsContent parseSmsContent(Bundle extra) {
        SmsContent sc = SendSmsUtil.parseContactName(extra);
        ((SceneSendSms) mSceneBase).setSmsContent(sc);
        return sc;
    }

    SmsContent getSmsContent() {
        if(LogUtil.DEBUG) {
            LogUtil.log(TAG, ((SceneSendSms) mSceneBase).getSmsContent().toString());
        }
        return ((SceneSendSms) mSceneBase).getSmsContent();
    }

    @Override
    public SceneStage next(String action, Bundle extra) {
        if (LogUtil.DEBUG) LogUtil.log(TAG, "action:" + action);
        parseSmsContent(extra);
        return null;
    }

    @Override
    public void action() {
        if(LogUtil.DEBUG) {
            LogUtil.log(TAG, "action : do nothing");
        }
    }
}
