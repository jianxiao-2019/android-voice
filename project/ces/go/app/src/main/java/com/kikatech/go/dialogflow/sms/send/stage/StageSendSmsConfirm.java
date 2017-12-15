package com.kikatech.go.dialogflow.sms.send.stage;

import android.os.Bundle;
import android.support.annotation.NonNull;

import com.kikatech.go.R;
import com.kikatech.go.dialogflow.SceneUtil;
import com.kikatech.go.dialogflow.sms.SmsContent;
import com.kikatech.go.dialogflow.sms.SmsUtil;
import com.kikatech.go.util.BackgroundThread;
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
    public void doAction() {
        onStageActionStart();
        action();
    }

    @Override
    public void action() {
        SmsContent sc = getSmsContent();

        if (LogUtil.DEBUG) LogUtil.log(TAG, "Send Message : \n" + sc.toString());

        Bundle args = new Bundle();
        args.putString(SceneUtil.EXTRA_EVENT, SceneUtil.EVENT_DISPLAY_MSG_SENT);
        args.putInt(SceneUtil.EXTRA_ALERT, R.raw.alert_succeed);
        send(args);

        SmsUtil.sendSms(mSceneBase.getContext(), sc.getChosenPhoneNumber(), sc.getMessageBody(true));
        BackgroundThread.getHandler().postDelayed(new Runnable() {
            @Override
            public void run() {
                exitScene();
            }
        }, SceneUtil.MSG_SENT_PAGE_DELAY);
    }
}