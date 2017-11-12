package com.kikatech.voice.dialogflow.telephony.outgoing.stage;

import android.os.Bundle;

import com.kikatech.voice.core.dialogflow.scene.ISceneFeedback;
import com.kikatech.voice.core.dialogflow.scene.SceneStage;

/**
 * Created by tianli on 17-11-11.
 */

public class StageAskName extends SceneStage {

    public StageAskName(ISceneFeedback feedback) {
        super(feedback);
    }

    @Override
    public SceneStage next(String action, Bundle extra) {
        return null;
    }

    @Override
    public void action() {

    }
}
