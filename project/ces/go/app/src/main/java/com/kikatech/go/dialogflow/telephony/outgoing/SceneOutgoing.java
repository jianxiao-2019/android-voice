package com.kikatech.go.dialogflow.telephony.outgoing;

import android.content.Context;
import android.os.Bundle;

import com.kikatech.voice.core.dialogflow.scene.ISceneFeedback;
import com.kikatech.voice.core.dialogflow.scene.SceneBase;
import com.kikatech.voice.core.dialogflow.scene.SceneStage;

/**
 * Created by tianli on 17-11-11.
 */

public class SceneOutgoing extends SceneBase {

    public static final String SCENE = "Telephony - Outgoing";

    public SceneOutgoing(Context context, ISceneFeedback feedback) {
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
        return new SceneStage(mFeedback) {
            @Override
            public SceneStage next(String action, Bundle extra) {
                return null;
            }

            @Override
            public void action() {
            }
        };
    }
}
