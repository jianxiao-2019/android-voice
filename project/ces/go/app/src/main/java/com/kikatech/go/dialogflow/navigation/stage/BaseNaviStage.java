package com.kikatech.go.dialogflow.navigation.stage;

import android.os.Bundle;
import android.support.annotation.NonNull;

import com.kikatech.go.dialogflow.navigation.NaviSceneActions;
import com.kikatech.go.util.LogUtil;
import com.kikatech.voice.core.dialogflow.scene.ISceneFeedback;
import com.kikatech.voice.core.dialogflow.scene.SceneBase;
import com.kikatech.voice.core.dialogflow.scene.SceneStage;

/**
 * Created by bradchang on 2017/11/14.
 */

public class BaseNaviStage extends SceneStage {

    BaseNaviStage(@NonNull SceneBase scene, ISceneFeedback feedback) {
        super(scene, feedback);
    }

    @Override
    public SceneStage next(String action, Bundle extra) {
        if (LogUtil.DEBUG) {
            LogUtil.log(TAG, "action:" + action);
        }
        if (NaviSceneActions.ACTION_NAV_CANCEL.equals(action)) {
            return new StageCancelNaviation(mSceneBase, mFeedback);
        }
        return null;
    }

    @Override
    public void prepare() {
    }

    @Override
    public void action() {
        onStageActionDone(false);
    }
}
