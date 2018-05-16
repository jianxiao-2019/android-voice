package com.kikatech.go.tutorial.dialogflow.stage;

import android.os.Bundle;
import android.support.annotation.NonNull;

import com.kikatech.go.tutorial.TutorialUtil;
import com.kikatech.go.tutorial.dialogflow.SceneTutorial;
import com.kikatech.go.tutorial.dialogflow.TutorialSceneActions;
import com.kikatech.go.util.LogUtil;
import com.kikatech.voice.core.dialogflow.scene.ISceneFeedback;
import com.kikatech.voice.core.dialogflow.scene.SceneBase;
import com.kikatech.voice.core.dialogflow.scene.SceneStage;

/**
 * @author SkeeterWang Created on 2018/5/11.
 */

class TStageNavDoneAlert extends BaseTutorialStage {
    protected final String TAG = "TStageNavDoneAlert";

    // 1-4-1
    TStageNavDoneAlert(@NonNull SceneBase scene, ISceneFeedback feedback) {
        super(scene, feedback);
    }

    @Override
    SceneStage getBackStage() {
        return new TStageNavDoneAlert(mSceneBase, mFeedback);
    }

    @Override
    SceneStage getNexStage(@NonNull String action, Bundle extra) {
        return null;
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
        // finish page
        // You got it!
        // Now enjoy your ride with KikaGo.
        // You can revisit this practice anytime in Settings.
        String[] content = TutorialUtil.getTutorialDone(mSceneBase.getContext());
        Bundle extras = new Bundle();
        extras.putInt(TutorialUtil.StageEvent.KEY_TYPE, TutorialUtil.StageActionType.ALERT_DONE);
        extras.putString(TutorialUtil.StageEvent.KEY_UI_TITLE, content[0]);
        extras.putString(TutorialUtil.StageEvent.KEY_UI_DESCRIPTION, content[1]);
        extras.putString(TutorialUtil.StageEvent.KEY_SCENE, SceneTutorial.SCENE);
        extras.putString(TutorialUtil.StageEvent.KEY_ACTION, TutorialSceneActions.ACTION_NAV_DONE);
        send(extras);
    }

    @Override
    public void onStageActionDone(boolean isInterrupted) {
        exitScene();
    }
}
