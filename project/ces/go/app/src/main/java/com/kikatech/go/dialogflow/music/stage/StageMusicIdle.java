package com.kikatech.go.dialogflow.music.stage;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.kikatech.go.dialogflow.music.MusicSceneUtil;
import com.kikatech.go.dialogflow.music.SceneActions;
import com.kikatech.voice.core.dialogflow.scene.ISceneFeedback;
import com.kikatech.voice.core.dialogflow.scene.SceneBase;
import com.kikatech.voice.core.dialogflow.scene.SceneStage;

/**
 * @author SkeeterWang Created on 2018/1/22.
 */

public class StageMusicIdle extends BaseMusicStage {
    private static final String TAG = "StageIdle";

    public StageMusicIdle(@NonNull SceneBase scene, ISceneFeedback feedback) {
        super(scene, feedback);
    }

    @Override
    protected int getAsrMode() {
        return super.getAsrMode();
    }

    @Override
    public SceneStage next(String action, Bundle extra) {
        if (!TextUtils.isEmpty(action)) {
            switch (action) {
                case SceneActions.ACTION_MUSIC_START:
                    String songName = MusicSceneUtil.parseSongName(extra);
                    return new StageQuerySong(mSceneBase, mFeedback, songName);
                case SceneActions.ACTION_MUSIC_SEARCH:
                    return new StageAskSong(mSceneBase, mFeedback);
                case SceneActions.ACTION_VOLUME_UP:
                    return new StageVolumeControl(mSceneBase, mFeedback, MusicSceneUtil.VolumeControlType.VOLUME_UP);
                case SceneActions.ACTION_VOLUME_DOWN:
                    return new StageVolumeControl(mSceneBase, mFeedback, MusicSceneUtil.VolumeControlType.VOLUME_DOWN);
                case SceneActions.ACTION_MUTE:
                    return new StageVolumeControl(mSceneBase, mFeedback, MusicSceneUtil.VolumeControlType.MUTE);
                case SceneActions.ACTION_UNMUTE:
                    return new StageVolumeControl(mSceneBase, mFeedback, MusicSceneUtil.VolumeControlType.UNMUTE);
            }
        }
        return null;
    }
}
