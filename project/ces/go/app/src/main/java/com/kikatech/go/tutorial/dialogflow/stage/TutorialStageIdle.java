package com.kikatech.go.tutorial.dialogflow.stage;

import android.os.Bundle;
import android.support.annotation.NonNull;

import com.kikatech.go.tutorial.dialogflow.TutorialSceneActions;
import com.kikatech.go.util.LogUtil;
import com.kikatech.voice.core.dialogflow.scene.ISceneFeedback;
import com.kikatech.voice.core.dialogflow.scene.SceneBase;
import com.kikatech.voice.core.dialogflow.scene.SceneStage;

/**
 * @author SkeeterWang Created on 2018/5/10.
 */

public class TutorialStageIdle extends BaseTutorialStage {
    protected final String TAG = "TutorialStageIdle";

    public TutorialStageIdle(@NonNull SceneBase scene, ISceneFeedback feedback) {
        super(scene, feedback);
    }

    @Override
    public SceneStage next(String action, Bundle extra) {
        return super.next(action, extra);
    }

    @Override
    SceneStage getBackStage() {
        return null;
    }

    @Override
    SceneStage getNexStage(@NonNull String action, Bundle extra) {
        switch (action) {
            case TutorialSceneActions.ACTION_NAV_START:
                // goto 1-2-1
                return new TStageNavAskCommandAlert(mSceneBase, mFeedback);
        }
        return super.onInputIncorrect();
    }

    @Override
    protected void prepare() {
        if (LogUtil.DEBUG) {
            LogUtil.logd(TAG, "prepare");
        }
    }

    @Override
    protected void action() {
        if (LogUtil.DEBUG) {
            LogUtil.logd(TAG, "action");
        }
    }
}
