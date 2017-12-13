package com.kikatech.go.dialogflow.common;

import android.content.Context;
import android.os.Bundle;

import com.kikatech.go.dialogflow.NonLoopSceneBase;
import com.kikatech.go.dialogflow.common.stage.StageAskAgainUncaught;
import com.kikatech.go.dialogflow.common.stage.StageAskAgainUnknown;
import com.kikatech.go.util.LogUtil;
import com.kikatech.voice.core.dialogflow.intent.Intent;
import com.kikatech.voice.core.dialogflow.scene.ISceneFeedback;
import com.kikatech.voice.core.dialogflow.scene.SceneStage;

/**
 * @author SkeeterWang Created on 2017/11/29.
 */

public class SceneCommon extends NonLoopSceneBase {

    public static final String SCENE = Intent.DEFAULT_SCENE;

    public SceneCommon(Context context, ISceneFeedback feedback) {
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
                if (Intent.ACTION_UNKNOWN.equals(action)) {
                    return new StageAskAgainUnknown(SceneCommon.this, mFeedback);
                } else if (Intent.ACTION_UNCAUGHT.equals(action)) {
                    return new StageAskAgainUncaught(SceneCommon.this, mFeedback);
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