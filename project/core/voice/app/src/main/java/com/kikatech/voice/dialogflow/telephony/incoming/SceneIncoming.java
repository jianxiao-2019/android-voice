package com.kikatech.voice.dialogflow.telephony.incoming;

import android.os.Bundle;

import com.kikatech.voice.core.dialogflow.scene.ISceneFeedback;
import com.kikatech.voice.core.dialogflow.scene.SceneBase;
import com.kikatech.voice.core.dialogflow.scene.SceneStage;
import com.kikatech.voice.dialogflow.telephony.incoming.stage.StageIncoming;

/**
 * Created by tianli on 17-11-11.
 */

public class SceneIncoming extends SceneBase {

    public static final String SCENE = "Telephony - Incoming";

    public SceneIncoming(ISceneFeedback feedback) {
        super(feedback);
    }

    @Override
    protected SceneStage init() {
        return new SceneStage(mFeedback) {
            @Override
            public SceneStage next(String action, Bundle extra) {
                if (SceneActions.ACTION_INCOMING_START.equals(action)) {
                    if (extra != null && extra.containsKey(SceneActions.PARAM_INCOMING_NAME)) {
                        return new StageIncoming(null, extra.getString(SceneActions.PARAM_INCOMING_NAME));
                    }
                }
                return null;
            }

            @Override
            public void action() {
            }
        };
    }
}
