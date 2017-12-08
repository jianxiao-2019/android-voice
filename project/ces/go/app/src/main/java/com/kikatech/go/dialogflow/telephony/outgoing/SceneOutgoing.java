package com.kikatech.go.dialogflow.telephony.outgoing;

import android.content.Context;

import com.kikatech.go.dialogflow.NonLoopSceneBase;
import com.kikatech.go.dialogflow.telephony.outgoing.stage.StageOutgoing;
import com.kikatech.voice.core.dialogflow.scene.ISceneFeedback;
import com.kikatech.voice.core.dialogflow.scene.SceneStage;

/**
 * Created by tianli on 17-11-11.
 */

public class SceneOutgoing extends NonLoopSceneBase {
    private static final String TAG = "SceneOutgoing";

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
        return new StageOutgoing(this, mFeedback);
    }
}