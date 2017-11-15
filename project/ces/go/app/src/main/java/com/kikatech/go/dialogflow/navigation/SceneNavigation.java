package com.kikatech.go.dialogflow.navigation;

import android.content.Context;

import com.kikatech.go.dialogflow.navigation.stage.StageNavigationIdle;
import com.kikatech.go.util.LogUtil;
import com.kikatech.voice.core.dialogflow.scene.ISceneFeedback;
import com.kikatech.voice.core.dialogflow.scene.SceneBase;
import com.kikatech.voice.core.dialogflow.scene.SceneStage;


/**
 * Created by tianli on 17-11-11.
 */

public class SceneNavigation extends SceneBase {

    public static final String SCENE = "Navigation";

    SceneNavigation(Context context, ISceneFeedback feedback) {
        super(context, feedback);
    }

    @Override
    protected String scene() {
        return SCENE;
    }

    @Override
    protected void onExit() {
        if(LogUtil.DEBUG) LogUtil.log("SceneNavigation", "onExit");
    }

    @Override
    protected SceneStage idle() {
        return new StageNavigationIdle(SceneNavigation.this, mFeedback);
    }
}