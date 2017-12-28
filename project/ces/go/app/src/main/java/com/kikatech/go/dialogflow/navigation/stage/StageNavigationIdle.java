package com.kikatech.go.dialogflow.navigation.stage;

import android.os.Bundle;

import com.kikatech.go.util.LogUtil;
import com.kikatech.voice.core.dialogflow.scene.ISceneFeedback;
import com.kikatech.voice.core.dialogflow.scene.SceneBase;
import com.kikatech.voice.core.dialogflow.scene.SceneStage;

/**
 * Created by tianli on 17-11-11.
 */

public class StageNavigationIdle extends BaseNaviStage {

    public StageNavigationIdle(SceneBase scene, ISceneFeedback feedback) {
        super(scene, feedback);
    }

    @Override
    public SceneStage next(String action, Bundle extra) {
        SceneStage superStage = super.next(action, extra);
        if (superStage != null) {
            return superStage;
        }
        return this;
    }

    @Override
    public void action() {
        if (LogUtil.DEBUG) LogUtil.log(TAG, "action");
    }
}