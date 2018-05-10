package com.kikatech.go.dialogflow.music.stage;

import android.os.Bundle;
import android.support.annotation.NonNull;

import com.kikatech.go.dialogflow.music.MusicSceneUtil;
import com.kikatech.go.services.MusicForegroundService;
import com.kikatech.voice.core.dialogflow.scene.ISceneFeedback;
import com.kikatech.voice.core.dialogflow.scene.SceneBase;
import com.kikatech.voice.core.dialogflow.scene.SceneStage;

/**
 * @author SkeeterWang Created on 2018/1/22.
 */

class StageVolumeControl extends BaseMusicStage {
    private static final String TAG = "StageVolumeControl";

    @MusicSceneUtil.VolumeControlType
    private int mVolumeControlType;

    StageVolumeControl(@NonNull SceneBase scene, ISceneFeedback feedback, @MusicSceneUtil.VolumeControlType int type) {
        super(scene, feedback);
        mVolumeControlType = type;
    }


    @Override
    public SceneStage next(String action, Bundle extra) {
        return null;
    }

    @Override
    public void action() {
        MusicForegroundService.processVolumeControl(mVolumeControlType);
    }

    @Override
    public void onStageActionDone(boolean isInterrupted) {
        exitScene();
    }
}
