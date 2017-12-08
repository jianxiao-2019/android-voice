package com.kikatech.go.dialogflow.stop;

import android.content.Context;

import com.kikatech.go.dialogflow.NonLoopSceneBase;
import com.kikatech.go.dialogflow.stop.stage.StageProcessStopIdle;
import com.kikatech.voice.core.dialogflow.scene.ISceneFeedback;
import com.kikatech.voice.core.dialogflow.scene.SceneStage;

/**
 * Created by bradchang on 2017/11/14.
 */

public class SceneStopIntent extends NonLoopSceneBase {

    public static final String SCENE = "Stop Intents";

    private Class<?> mMainUIClass;

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

    public void setMainUIClass(Class<?> mainUIClass) {
        mMainUIClass = mainUIClass;
    }

    public Class<?> getMainUIClass() {
        return mMainUIClass;
    }

    @Override
    protected SceneStage idle() {
        return new StageProcessStopIdle(this, mFeedback);
    }
}