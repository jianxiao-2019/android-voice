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

    boolean mStopNavi = false;

    BaseNaviStage(@NonNull SceneBase scene, ISceneFeedback feedback) {
        super(scene, feedback);
    }

    @Override
    public SceneStage next(String action, Bundle extra) {
        if (LogUtil.DEBUG) LogUtil.log(TAG, "action:" + action);
        mStopNavi = action.equals(NaviSceneActions.ACTION_NAV_CANCEL);
        if (mStopNavi) {
            speak("OK, Stop navigation !");
            exitScene();
        }
        return null;
    }

    @Override
    public void prepare() {
    }

    @Override
    public void action() {
    }
}
