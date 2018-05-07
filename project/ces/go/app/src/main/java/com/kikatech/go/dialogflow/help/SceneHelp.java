package com.kikatech.go.dialogflow.help;

import android.content.Context;

import com.kikatech.go.dialogflow.NonLoopSceneBase;
import com.kikatech.go.dialogflow.help.stage.StageHelpIdle;
import com.kikatech.voice.core.dialogflow.scene.ISceneFeedback;
import com.kikatech.voice.core.dialogflow.scene.SceneStage;

/**
 * @author SkeeterWang Created on 2018/5/2.
 */

public class SceneHelp extends NonLoopSceneBase {

    public static final String SCENE = "Help";

    SceneHelp(Context context, ISceneFeedback feedback) {
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
        return new StageHelpIdle(this, mFeedback);
    }
}
