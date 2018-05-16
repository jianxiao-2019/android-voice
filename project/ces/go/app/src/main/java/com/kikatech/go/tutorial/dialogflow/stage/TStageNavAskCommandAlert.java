package com.kikatech.go.tutorial.dialogflow.stage;

import android.os.Bundle;
import android.support.annotation.NonNull;

import com.kikatech.go.dialogflow.model.OptionList;
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

class TStageNavAskCommandAlert extends BaseTutorialStage {
    protected final String TAG = "TStageNavAskCommandAlert";

    // 1-2-1
    TStageNavAskCommandAlert(@NonNull SceneBase scene, ISceneFeedback feedback) {
        super(scene, feedback);
    }

    @Override
    public SceneStage next(String action, Bundle extra) {
        return super.next(action, extra);
    }

    @Override
    SceneStage getBackStage() {
        return new TStageNavAskCommandAlert(mSceneBase, mFeedback);
    }

    @Override
    SceneStage getNexStage(@NonNull String action, Bundle extra) {
        switch (action) {
            case TutorialSceneActions.ACTION_NAV_ASK_COMMAND: // user press button ok
                return new TStageNavAskCommand(mSceneBase, mFeedback);
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
        // alert
        // After the beep say "Navigate" to continue
        String[] content = TutorialUtil.getAskCommand(mSceneBase.getContext());
        Bundle extras = new Bundle();
        extras.putInt(TutorialUtil.StageEvent.KEY_TYPE, TutorialUtil.StageActionType.ALERT);
        extras.putParcelable(TutorialUtil.StageEvent.KEY_OPTION_LIST, OptionList.getDefaultOptionList());
        extras.putInt(TutorialUtil.StageEvent.KEY_UI_INDEX, 2);
        extras.putString(TutorialUtil.StageEvent.KEY_UI_TITLE, content[0]);
        extras.putString(TutorialUtil.StageEvent.KEY_UI_DESCRIPTION, content[1]);
        extras.putString(TutorialUtil.StageEvent.KEY_SCENE, SceneTutorial.SCENE);
        extras.putString(TutorialUtil.StageEvent.KEY_ACTION, TutorialSceneActions.ACTION_NAV_ASK_COMMAND);
        send(extras);
    }
}
