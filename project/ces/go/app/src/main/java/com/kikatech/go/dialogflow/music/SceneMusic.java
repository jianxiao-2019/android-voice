package com.kikatech.go.dialogflow.music;

import android.content.Context;

import com.kikatech.go.dialogflow.NonLoopSceneBase;
import com.kikatech.go.dialogflow.music.stage.StageMusicIdle;
import com.kikatech.voice.core.dialogflow.scene.ISceneFeedback;
import com.kikatech.voice.core.dialogflow.scene.SceneStage;

/**
 * @author SkeeterWang Created on 2018/1/5.
 */

public class SceneMusic extends NonLoopSceneBase {
    private static final String TAG = "SceneMusic";

    public static final String SCENE = "Music";

    public SceneMusic(Context context, ISceneFeedback feedback) {
        super(context, feedback);
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
        return new StageMusicIdle(this, mFeedback);
    }
}
