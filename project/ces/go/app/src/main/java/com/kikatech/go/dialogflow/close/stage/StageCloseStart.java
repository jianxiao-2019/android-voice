package com.kikatech.go.dialogflow.close.stage;

import android.os.Bundle;
import android.support.annotation.NonNull;

import com.kikatech.go.services.DialogFlowForegroundService;
import com.kikatech.voice.core.dialogflow.scene.ISceneFeedback;
import com.kikatech.voice.core.dialogflow.scene.SceneBase;
import com.kikatech.voice.core.dialogflow.scene.SceneStage;

/**
 * @author SkeeterWang Created on 2018/5/7.
 */

public class StageCloseStart extends BaseCloseStage {

    StageCloseStart(@NonNull SceneBase scene, ISceneFeedback feedback) {
        super(scene, feedback);
    }

    @Override
    public SceneStage next(String action, Bundle extra) {
        return null;
    }

    @Override
    public void onStageActionDone(boolean isInterrupted) {
        DialogFlowForegroundService.processStop(mSceneBase.getContext(), DialogFlowForegroundService.class);
    }
}