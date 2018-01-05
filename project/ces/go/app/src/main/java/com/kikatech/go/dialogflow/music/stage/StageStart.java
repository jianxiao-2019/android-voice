package com.kikatech.go.dialogflow.music.stage;

import android.os.Bundle;
import android.support.annotation.NonNull;

import com.kikatech.go.music.MusicManager;
import com.kikatech.voice.core.dialogflow.scene.ISceneFeedback;
import com.kikatech.voice.core.dialogflow.scene.SceneBase;
import com.kikatech.voice.core.dialogflow.scene.SceneStage;

/**
 * @author SkeeterWang Created on 2018/1/5.
 */

public class StageStart extends BaseMusicStage {

    public StageStart(@NonNull SceneBase scene, ISceneFeedback feedback) {
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
    public void action() {
        MusicManager.getIns().play();
    }

    @Override
    public void onStageActionDone(boolean isInterrupted, boolean delayAsrResume) {
        exitScene();
    }
}
