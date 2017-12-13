package com.kikatech.go.dialogflow.stop.stage;

import android.os.Bundle;
import android.support.annotation.NonNull;

import com.kikatech.go.dialogflow.BaseSceneStage;
import com.kikatech.go.dialogflow.navigation.NaviSceneUtil;
import com.kikatech.go.dialogflow.stop.SceneActions;
import com.kikatech.go.ui.KikaAlphaUiActivity;
import com.kikatech.go.util.LogUtil;
import com.kikatech.voice.core.dialogflow.scene.ISceneFeedback;
import com.kikatech.voice.core.dialogflow.scene.SceneBase;
import com.kikatech.voice.core.dialogflow.scene.SceneStage;

/**
 * Created by bradchang on 2017/11/15.
 */

public class StageProcessStopIdle extends BaseSceneStage {

    private final static String ACTION_KEY = "stop";
    private final static String STOP_TARGET_NAVIGATE = "\"navigate\"";

    public StageProcessStopIdle(@NonNull SceneBase scene, ISceneFeedback feedback) {
        super(scene, feedback);
    }

    @Override
    public SceneStage next(String action, Bundle extra) {
        if(SceneActions.STOP_ACTION.equals(action)) {
            String target = extra.getString(ACTION_KEY, "");
            if(LogUtil.DEBUG) LogUtil.log("StageProcessStopIdle", "target:" + target);

            if(STOP_TARGET_NAVIGATE.equals(target)) {
                return new StageStopNavigation(mSceneBase, mFeedback);
            } else {
                return new StageStop(mSceneBase, mFeedback);
            }
        } else {
            if(LogUtil.DEBUG) LogUtil.log("StageProcessStopIdle", "Unsupported action:" + action);
        }

        return null;
    }

    @Override
    public void prepare() {
    }

    @Override
    public void action() {
        if(LogUtil.DEBUG) LogUtil.log("StageProcessStopIdle", "action");
    }
}
