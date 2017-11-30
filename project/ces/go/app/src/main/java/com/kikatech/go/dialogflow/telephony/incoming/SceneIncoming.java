package com.kikatech.go.dialogflow.telephony.incoming;

import android.content.Context;
import android.os.Bundle;

import com.kikatech.go.dialogflow.NonLoopSceneBase;
import com.kikatech.go.dialogflow.telephony.incoming.stage.StageIncoming;
import com.kikatech.voice.core.dialogflow.scene.ISceneFeedback;
import com.kikatech.voice.core.dialogflow.scene.SceneStage;

/**
 * Created by tianli on 17-11-11.
 */

public class SceneIncoming extends NonLoopSceneBase {

    public static final String SCENE = "Telephony - Incoming";

    public SceneIncoming(Context context, ISceneFeedback feedback) {
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
                if (SceneActions.ACTION_INCOMING_START.equals(action)) {
                    if (extra != null && extra.containsKey(SceneActions.PARAM_INCOMING_NAME)) {
                        return new StageIncoming(SceneIncoming.this, mFeedback, extra.getString(SceneActions.PARAM_INCOMING_NAME));
                    }
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
