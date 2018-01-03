package com.kikatech.go.dialogflow.ces.demo.wakeup;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;

import com.kikatech.go.dialogflow.NonLoopSceneBase;
import com.kikatech.go.dialogflow.ces.demo.wakeup.stage.StageWakeUpFunny;
import com.kikatech.go.util.LogUtil;
import com.kikatech.voice.core.dialogflow.scene.ISceneFeedback;
import com.kikatech.voice.core.dialogflow.scene.SceneStage;

/**
 * @author SkeeterWang Created on 2018/1/2.
 */

public class SceneWakeUp extends NonLoopSceneBase {
    private static final String TAG = "SceneWakeUp";

    public static final String SCENE = "WakeUp";

    SceneWakeUp(Context context, ISceneFeedback feedback) {
        super(context, feedback);
        if (LogUtil.DEBUG) {
            LogUtil.logv(TAG, "constructor");
        }
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
        if (LogUtil.DEBUG) {
            LogUtil.logv(TAG, "idle");
        }
        return new SceneStage(this, mFeedback) {
            @Override
            public SceneStage next(String action, Bundle extra) {
                if (!TextUtils.isEmpty(action)) {
                    switch (action) {
                        case WakeUpSceneAction.ACTION_WAKE_UP_FUNNY:
                            return new StageWakeUpFunny(mSceneBase, mFeedback);
                    }
                }
                return null;
            }

            @Override
            protected void prepare() {

            }

            @Override
            protected void action() {

            }
        };
    }
}
