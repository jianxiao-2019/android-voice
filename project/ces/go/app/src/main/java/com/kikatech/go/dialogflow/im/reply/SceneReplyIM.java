package com.kikatech.go.dialogflow.im.reply;

import android.content.Context;

import com.kikatech.go.dialogflow.im.send.stage.StageIdle;
import com.kikatech.voice.core.dialogflow.scene.ISceneFeedback;
import com.kikatech.voice.core.dialogflow.scene.SceneBase;
import com.kikatech.voice.core.dialogflow.scene.SceneStage;

/**
 * Created by brad_chang on 2017/11/23.
 */

public class SceneReplyIM extends SceneBase {

    public static final String SCENE = "Reply IM";

    public SceneReplyIM(Context context, ISceneFeedback feedback) {
        super(context, feedback);
    }

    @Override
    protected String scene() {
        return null;
    }

    @Override
    protected void onExit() {

    }

    @Override
    protected SceneStage idle() {
        return new StageIdle(this, mFeedback);
    }
}