package com.kikatech.go.dialogflow.stop.stage;

import android.os.Bundle;
import android.support.annotation.NonNull;

import com.kikatech.go.dialogflow.BaseSceneStage;
import com.kikatech.go.dialogflow.SceneUtil;
import com.kikatech.go.dialogflow.navigation.NaviSceneUtil;
import com.kikatech.go.dialogflow.stop.SceneStopIntent;
import com.kikatech.go.ui.KikaAlphaUiActivity;
import com.kikatech.voice.core.dialogflow.scene.ISceneFeedback;
import com.kikatech.voice.core.dialogflow.scene.SceneBase;
import com.kikatech.voice.core.dialogflow.scene.SceneStage;

/**
 * Created by brad_chang on 2017/11/21.
 */

public class StageStopNavigation extends BaseSceneStage {
    public StageStopNavigation(@NonNull SceneBase scene, ISceneFeedback feedback) {
        super(scene, feedback);
    }

    @Override
    public SceneStage next(String action, Bundle extra) {
        return null;
    }

    @Override
    public void prepare() {
    }

    @Override
    public void doAction() {
        onStageActionStart();
        action();
    }

    @Override
    public void action() {
        String ttsText = SceneUtil.getStopNavigation(mSceneBase.getContext());
        speak(ttsText);
    }

    @Override
    public void onStageActionDone(boolean isInterrupted) {
        //NaviSceneUtil.stopNavigation(mSceneBase.getContext(), ((SceneStopIntent) mSceneBase).getMainUIClass());
        NaviSceneUtil.stopNavigation(mSceneBase.getContext(), KikaAlphaUiActivity.class);
        exitScene();
    }
}
