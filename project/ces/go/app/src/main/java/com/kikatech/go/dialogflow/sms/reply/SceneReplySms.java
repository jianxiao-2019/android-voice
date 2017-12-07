package com.kikatech.go.dialogflow.sms.reply;

import android.content.Context;
import android.support.annotation.NonNull;

import com.kikatech.go.dialogflow.NonLoopSceneBase;
import com.kikatech.go.dialogflow.im.reply.ReplyIMMessage;
import com.kikatech.go.dialogflow.sms.ReplySmsMessage;
import com.kikatech.go.dialogflow.sms.reply.stage.StageIdle;
import com.kikatech.go.message.im.BaseIMObject;
import com.kikatech.go.message.sms.SmsObject;
import com.kikatech.voice.core.dialogflow.scene.ISceneFeedback;
import com.kikatech.voice.core.dialogflow.scene.SceneStage;

/**
 * Created by brad_chang on 2017/11/17.
 */

public class SceneReplySms extends NonLoopSceneBase {

    public static final String SCENE = "ReplySMS";

    private final ReplySmsMessage mReplySmsMessage = new ReplySmsMessage();

    public interface ISmsFunc {
        SmsObject getReceivedSms(long t);
    }

    private final ISmsFunc mSmsFunc;

    public SceneReplySms(Context context, ISceneFeedback feedback, @NonNull ISmsFunc smsFunc) {
        super(context, feedback);
        mSmsFunc = smsFunc;
    }

    public ReplySmsMessage getReplyMessage() {
        return mReplySmsMessage;
    }

    public void updateSmsContent(SmsObject sms) {
        mReplySmsMessage.updateSmsObject(sms);
    }

    public void updateEmoji(String emojiJson) {
        mReplySmsMessage.updateEmoji(emojiJson);
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

    @Override
    protected boolean supportEmoji() {
        return true;
    }
}
