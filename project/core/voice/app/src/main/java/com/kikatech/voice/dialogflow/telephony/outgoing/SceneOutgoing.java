package com.kikatech.voice.dialogflow.telephony.outgoing;

import android.os.Bundle;

import com.kikatech.voice.core.dialogflow.scene.ISceneFeedback;
import com.kikatech.voice.core.dialogflow.scene.SceneBase;
import com.kikatech.voice.core.dialogflow.scene.SceneStage;

/**
 * Created by tianli on 17-11-11.
 */

public class SceneOutgoing extends SceneBase {

    public static final String SCENE = "Telephony - Outgoing";

    public SceneOutgoing(ISceneFeedback feedback) {
        super(feedback);
    }

    @Override
    protected SceneStage init() {
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
