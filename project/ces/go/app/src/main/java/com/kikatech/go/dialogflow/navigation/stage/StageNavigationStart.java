package com.kikatech.go.dialogflow.navigation.stage;

import android.os.Bundle;

import com.kikatech.voice.core.dialogflow.scene.ISceneFeedback;
import com.kikatech.voice.core.dialogflow.scene.SceneStage;

/**
 * Created by tianli on 17-11-11.
 */

public class StageNavigationStart extends SceneStage {

    public StageNavigationStart(ISceneFeedback feedback) {
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
