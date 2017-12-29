package com.kikatech.go.dialogflow.sms.reply.stage;

import android.os.Bundle;
import android.support.annotation.NonNull;

import com.kikatech.go.R;
import com.kikatech.go.dialogflow.SceneUtil;
import com.kikatech.go.dialogflow.sms.ReplySmsMessage;
import com.kikatech.go.dialogflow.sms.SmsUtil;
import com.kikatech.go.util.BackgroundThread;
import com.kikatech.go.util.LogUtil;
import com.kikatech.voice.core.dialogflow.scene.ISceneFeedback;
import com.kikatech.voice.core.dialogflow.scene.SceneBase;

/**
 * Created by brad_chang on 2017/11/20.
 */

public class SendMessageReplySmsStage extends BaseReplySmsStage {

    SendMessageReplySmsStage(@NonNull SceneBase scene, ISceneFeedback feedback) {
        super(scene, feedback);
    }

    @Override
    public void doAction() {
        onStageActionStart();
        action();
    }

    @Override
    public void action() {
        ReplySmsMessage msg = getReplyMessage();
        if (LogUtil.DEBUG) {
            LogUtil.log(TAG, "Send Message : \n" + msg.getMessageBody() + ", phone:" + msg.getPhoneNumber());
        }
        Bundle args = new Bundle();
        args.putString(SceneUtil.EXTRA_EVENT, SceneUtil.EVENT_DISPLAY_MSG_SENT);
        args.putBoolean(SceneUtil.EXTRA_SEND_SUCCESS, true);
        args.putInt(SceneUtil.EXTRA_ALERT, R.raw.alert_succeed);
        send(args);
        SmsUtil.sendSms(mSceneBase.getContext(), msg.getPhoneNumber(), msg.getMessageBody(true));
        BackgroundThread.getHandler().postDelayed(new Runnable() {
            @Override
            public void run() {
                exitScene();
            }
        }, SceneUtil.MSG_SENT_PAGE_DELAY);
    }
}