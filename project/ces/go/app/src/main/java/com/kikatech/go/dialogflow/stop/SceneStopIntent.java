package com.kikatech.go.dialogflow.stop;

import android.content.Context;

import com.kikatech.go.dialogflow.stop.stage.StageProcessStopIdle;
import com.kikatech.voice.core.dialogflow.scene.ISceneFeedback;
import com.kikatech.voice.core.dialogflow.scene.SceneBase;
import com.kikatech.voice.core.dialogflow.scene.SceneStage;

/**
 * Created by bradchang on 2017/11/14.
 */

public class SceneStopIntent extends SceneBase {

    public static final String SCENE = "Stop Intents";

    SceneStopIntent(Context context, ISceneFeedback feedback) {
        super(context, feedback);
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
        return new StageProcessStopIdle(this, mFeedback);
    }
}
