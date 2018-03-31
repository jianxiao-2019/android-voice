package com.kikatech.go.dialogflow.error;

import android.content.Context;
import android.os.Bundle;

import com.kikatech.go.dialogflow.NonLoopSceneBase;
import com.kikatech.go.dialogflow.error.stage.StageErrorDFEngine;
import com.kikatech.go.dialogflow.error.stage.StageErrorServerConnection;
import com.kikatech.go.util.LogUtil;
import com.kikatech.voice.core.dialogflow.scene.ISceneFeedback;
import com.kikatech.voice.core.dialogflow.scene.SceneStage;

/**
 * @author SkeeterWang Created on 2018/3/31.
 */

public class SceneError extends NonLoopSceneBase {

    public static final String SCENE = "Error";

    public SceneError(Context context, ISceneFeedback feedback) {
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
        return new SceneStage(this, mFeedback) {
            @Override
            public SceneStage next(String action, Bundle extra) {
                if (LogUtil.DEBUG) {
                    LogUtil.log("SceneBase", "next: action: " + action);
                }
                if (action == null) {
                    return null;
                }
                switch (action) {
                    case ErrorSceneActions.ACTION_SERVER_CONNECTION_ERROR:
                        return new StageErrorServerConnection(mSceneBase, mFeedback);
                    case ErrorSceneActions.ACTION_DF_ENGINE_ERROR:
                        return new StageErrorDFEngine(mSceneBase, mFeedback);
                }
                return null;
            }

            @Override
            public void prepare() {
            }

            @Override
            public void action() {
            }
        };
    }
}
