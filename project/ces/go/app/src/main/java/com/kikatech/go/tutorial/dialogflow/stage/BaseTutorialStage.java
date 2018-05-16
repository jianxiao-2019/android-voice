package com.kikatech.go.tutorial.dialogflow.stage;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.kikatech.go.dialogflow.BaseSceneStage;
import com.kikatech.go.tutorial.dialogflow.TutorialSceneActions;
import com.kikatech.go.util.LogUtil;
import com.kikatech.voice.core.dialogflow.intent.Intent;
import com.kikatech.voice.core.dialogflow.scene.ISceneFeedback;
import com.kikatech.voice.core.dialogflow.scene.SceneBase;
import com.kikatech.voice.core.dialogflow.scene.SceneStage;

/**
 * @author SkeeterWang Created on 2018/5/10.
 */

abstract class BaseTutorialStage extends BaseSceneStage {

    BaseTutorialStage(@NonNull SceneBase scene, ISceneFeedback feedback) {
        super(scene, feedback);
        overrideUncaughtAction = true;
        overrideUnknownAction = true;
    }

    @Override
    public SceneStage next(String action, Bundle extra) {
        if (!TextUtils.isEmpty(action)) {
            if (LogUtil.DEBUG) {
                LogUtil.logv(TAG, String.format("action: %s", action));
            }
            switch (action) {
                case Intent.ACTION_UNKNOWN:
                case Intent.ACTION_UNCAUGHT:
                case TutorialSceneActions.ACTION_STOP:
                case TutorialSceneActions.ACTION_NAV_BACK:
                    SceneStage backStage = getBackStage();
                    if (LogUtil.DEBUG) {
                        String log = backStage != null ? TAG : "null";
                        LogUtil.logv(TAG, String.format("backStage: %s", log));
                    }
                    return backStage;
            }
            SceneStage nextStage = getNexStage(action, extra);
            if (LogUtil.DEBUG) {
                String log = nextStage != null ? TAG : "null";
                LogUtil.logv(TAG, String.format("nextStage: %s", log));
            }
            return nextStage;
        } else {
            if (LogUtil.DEBUG) {
                LogUtil.logw(TAG, "action empty");
            }
            return onInputIncorrect();
        }
    }

    SceneStage onInputIncorrect() {
        if (LogUtil.DEBUG) {
            LogUtil.logw(TAG, "onInputIncorrect");
        }
        return this;
    }

    abstract SceneStage getBackStage();

    abstract SceneStage getNexStage(@NonNull String action, Bundle extra);
}
