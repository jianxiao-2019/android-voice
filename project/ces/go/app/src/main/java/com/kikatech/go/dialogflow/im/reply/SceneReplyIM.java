package com.kikatech.go.dialogflow.im.reply;

import android.content.Context;
import android.support.annotation.NonNull;

import com.kikatech.go.dialogflow.NonLoopSceneBase;
import com.kikatech.go.dialogflow.im.reply.stage.ReplyIMStageIdle;
import com.kikatech.go.message.im.BaseIMObject;
import com.kikatech.voice.core.dialogflow.scene.ISceneFeedback;
import com.kikatech.voice.core.dialogflow.scene.SceneStage;

/**
 * Created by brad_chang on 2017/11/23.
 */

public class SceneReplyIM extends NonLoopSceneBase {

    public static final String SCENE = "Reply IM";
    private final ReplyIMMessage mReplyIMMessage = new ReplyIMMessage();

    public interface IImFunc {
        BaseIMObject getReceivedIM(long t);
    }

    private IImFunc mIImFunc;

    public SceneReplyIM(Context context, ISceneFeedback feedback, @NonNull IImFunc imFunc) {
        super(context, feedback);
        mIImFunc = imFunc;
    }

    public BaseIMObject getReceivedIM(long t) {
        return mIImFunc.getReceivedIM(t);
    }

    public ReplyIMMessage getReplyMessage() {
        return mReplyIMMessage;
    }

    public void updateIMContent(BaseIMObject imo) {
        mReplyIMMessage.updateIMObject(imo);
    }

    public void updateEmoji(String emojiJson) {
        mReplyIMMessage.updateEmoji(emojiJson);
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
        return new ReplyIMStageIdle(this, mFeedback);
    }
}