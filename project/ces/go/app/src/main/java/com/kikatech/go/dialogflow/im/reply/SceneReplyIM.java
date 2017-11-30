package com.kikatech.go.dialogflow.im.reply;

import android.content.Context;
import android.support.annotation.NonNull;

import com.kikatech.go.dialogflow.NonLoopSceneBase;
import com.kikatech.go.dialogflow.im.reply.stage.StageIdle;
import com.kikatech.go.message.im.BaseIMObject;
import com.kikatech.voice.core.dialogflow.scene.ISceneFeedback;
import com.kikatech.voice.core.dialogflow.scene.SceneStage;

/**
 * Created by brad_chang on 2017/11/23.
 */

public class SceneReplyIM extends NonLoopSceneBase {

    public static final String SCENE = "Reply IM";

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