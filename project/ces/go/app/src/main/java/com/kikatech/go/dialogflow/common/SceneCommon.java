package com.kikatech.go.dialogflow.common;

import android.content.Context;
import android.os.Bundle;

import com.kikatech.go.dialogflow.common.stage.StageAskAgain;
import com.kikatech.voice.core.dialogflow.intent.Intent;
import com.kikatech.voice.core.dialogflow.scene.ISceneFeedback;
import com.kikatech.voice.core.dialogflow.scene.SceneBase;
import com.kikatech.voice.core.dialogflow.scene.SceneStage;

/**
 * @author SkeeterWang Created on 2017/11/29.
 */

public class SceneCommon extends SceneBase {

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
                if (Intent.ACTION_UNKNOWN.equals(action)) {
                    return new StageAskAgain(SceneCommon.this, mFeedback);
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