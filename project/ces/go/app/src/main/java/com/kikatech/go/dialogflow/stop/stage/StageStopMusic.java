package com.kikatech.go.dialogflow.stop.stage;

import android.os.Bundle;
import android.support.annotation.NonNull;

import com.kikatech.go.dialogflow.BaseSceneStage;
import com.kikatech.go.services.MusicForegroundService;
import com.kikatech.go.ui.KikaMultiDexApplication;
import com.kikatech.voice.core.dialogflow.scene.ISceneFeedback;
import com.kikatech.voice.core.dialogflow.scene.SceneBase;
import com.kikatech.voice.core.dialogflow.scene.SceneStage;

/**
 * @author SkeeterWang Created on 2018/1/5.
 */

public class StageStopMusic extends BaseSceneStage {

    StageStopMusic(@NonNull SceneBase scene, ISceneFeedback feedback) {
        super(scene, feedback);
    }

    @Override
    public SceneStage next(String action, Bundle extra) {
        return null;
    }

    @Override
    protected void prepare() {
    }

    @Override
    public void action() {
        MusicForegroundService.stopMusic(KikaMultiDexApplication.getAppContext());
    }

    @Override
    public void onStageActionDone(boolean isInterrupted, boolean delayAsrResume) {
        exitScene();
    }
}
