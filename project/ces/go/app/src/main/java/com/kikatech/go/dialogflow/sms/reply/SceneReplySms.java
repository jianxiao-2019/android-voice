package com.kikatech.go.dialogflow.sms.reply;

import android.content.Context;
import android.support.annotation.NonNull;

import com.kikatech.go.dialogflow.sms.reply.stage.StageIdle;
import com.kikatech.go.message.sms.SmsObject;
import com.kikatech.voice.core.dialogflow.scene.ISceneFeedback;
import com.kikatech.voice.core.dialogflow.scene.SceneBase;
import com.kikatech.voice.core.dialogflow.scene.SceneStage;

/**
 * Created by brad_chang on 2017/11/17.
 */

public class SceneReplySms extends SceneBase {

    public static final String SCENE = "ReplySMS";

    public interface ISmsFunc {
        SmsObject getReceivedSms(long t);
    }

    private final ISmsFunc mSmsFunc;

    public SceneReplySms(Context context, ISceneFeedback feedback, @NonNull ISmsFunc smsFunc) {
        super(context, feedback);
        mSmsFunc = smsFunc;
    }

    public SmsObject getReceivedSms(long t) {
        return mSmsFunc.getReceivedSms(t);
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
        return new StageIdle(this, mFeedback);
    }
}
