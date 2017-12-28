package com.kikatech.go.dialogflow.gotomain;

import android.content.Context;

import com.kikatech.go.dialogflow.NonLoopSceneBase;
import com.kikatech.go.dialogflow.gotomain.stage.StageGotoMainIdle;
import com.kikatech.voice.core.dialogflow.scene.ISceneFeedback;
import com.kikatech.voice.core.dialogflow.scene.SceneStage;

/**
 * Created by brad_chang on 2017/12/28.
 */

public class SceneGotoMain extends NonLoopSceneBase {

    public static final String SCENE = "GotoMain";

    public SceneGotoMain(Context context, ISceneFeedback feedback) {
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
        return new StageGotoMainIdle(this, mFeedback);
    }
}
