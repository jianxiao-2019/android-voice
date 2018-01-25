package com.kikatech.go.dialogflow.stop.stage;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.kikatech.go.dialogflow.BaseSceneStage;
import com.kikatech.go.dialogflow.stop.SceneActions;
import com.kikatech.go.util.LogUtil;
import com.kikatech.voice.core.dialogflow.intent.Intent;
import com.kikatech.voice.core.dialogflow.scene.ISceneFeedback;
import com.kikatech.voice.core.dialogflow.scene.SceneBase;
import com.kikatech.voice.core.dialogflow.scene.SceneStage;

/**
 * Created by bradchang on 2017/11/15.
 */

public class StageProcessStopIdle extends BaseSceneStage {

    private final static String ACTION_KEY = "stop";
    private final static String STOP_TARGET_NAVIGATE = "\"navigate\"";
    private final static String STOP_TARGET_MUSIC = "\"music\"";

    public StageProcessStopIdle(@NonNull SceneBase scene, ISceneFeedback feedback) {
        super(scene, feedback);
    }

    @Override
    public SceneStage next(String action, Bundle extra) {
        if (!TextUtils.isEmpty(action)) {
            switch (action) {
                case SceneActions.STOP_ACTION:
                    String target = extra.getString(ACTION_KEY, "");

                    if (LogUtil.DEBUG) {
                        LogUtil.log("StageProcessStopIdle", "target:" + target);
                    }

                    setQueryAnyWords(false);

                    if (STOP_TARGET_NAVIGATE.equals(target)) {
                        return new StageStopNavigation(mSceneBase, mFeedback);
                    } else if (STOP_TARGET_MUSIC.equals(target)) {
                        return new StageStopMusic(mSceneBase, mFeedback);
                    } else {
                        return new StageStop(mSceneBase, mFeedback, Intent.parseUserInput(extra));
                    }
                default:
                    if (LogUtil.DEBUG) {
                        LogUtil.log("StageProcessStopIdle", "Unsupported action:" + action);
                    }
                    break;
            }
        }
        return null;
    }

    @Override
    public void prepare() {
    }

    @Override
    public void action() {
        if (LogUtil.DEBUG) LogUtil.log("StageProcessStopIdle", "action");
    }
}
