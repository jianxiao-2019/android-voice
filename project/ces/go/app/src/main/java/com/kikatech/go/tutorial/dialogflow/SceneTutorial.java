package com.kikatech.go.tutorial.dialogflow;

import android.content.Context;

import com.kikatech.go.tutorial.dialogflow.stage.TutorialStageIdle;
import com.kikatech.go.util.LogUtil;
import com.kikatech.voice.core.dialogflow.scene.ISceneFeedback;
import com.kikatech.voice.core.dialogflow.scene.SceneBase;
import com.kikatech.voice.core.dialogflow.scene.SceneStage;

/**
 * @author SkeeterWang Created on 2018/5/10.
 */

public class SceneTutorial extends SceneBase {
    public static final String SCENE = "Tutorial";

    SceneTutorial(Context context, ISceneFeedback feedback) {
        super(context, feedback);
    }

    @Override
    protected String scene() {
        return SCENE;
    }

    @Override
    protected void onExit() {
        if (LogUtil.DEBUG) {
            LogUtil.log("SceneTutorial", "onExit");
        }
    }

    @Override
    protected SceneStage idle() {
        return new TutorialStageIdle(this, mFeedback);
    }

    @Override
    protected SceneStage onOverCounts() {
        return null;
    }

    @Override
    protected int getMaxStageStayCount() {
        return -1;
    }
}