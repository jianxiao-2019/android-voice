package com.kikatech.go.dialogflow.music.stage;

import android.os.Bundle;
import android.support.annotation.NonNull;

import com.kikatech.go.dialogflow.SceneUtil;
import com.kikatech.go.dialogflow.model.TtsText;
import com.kikatech.go.music.model.YouTubeVideoList;
import com.kikatech.go.services.MusicForegroundService;
import com.kikatech.voice.core.dialogflow.scene.ISceneFeedback;
import com.kikatech.voice.core.dialogflow.scene.SceneBase;
import com.kikatech.voice.core.dialogflow.scene.SceneStage;

/**
 * @author SkeeterWang Created on 2018/1/17.
 */

class StagePlaySong extends BaseMusicStage {
    private static final String TAG = "StagePlaySong";

    private YouTubeVideoList mPlayList;

    StagePlaySong(@NonNull SceneBase scene, ISceneFeedback feedback, YouTubeVideoList playList) {
        super(scene, feedback);
        mPlayList = playList;
    }

    @Override
    public SceneStage next(String action, Bundle extra) {
        return null;
    }

    @Override
    public void doAction() {
        onStageActionStart();
        action();
    }

    @Override
    public void action() {
        if (mPlayList != null && !mPlayList.isEmpty()) {
            String[] uiAndTtsText = SceneUtil.getPlayMusic(mSceneBase.getContext());
            if (uiAndTtsText.length > 0) {
                String uiText = uiAndTtsText[0];
                String ttsText = uiAndTtsText[1];
                TtsText tText = new TtsText(SceneUtil.ICON_MUSIC, uiText);
                Bundle args = new Bundle();
                args.putParcelable(SceneUtil.EXTRA_TTS_TEXT, tText);
                speak(ttsText, args);
            }
            MusicForegroundService.startMusic(mSceneBase.getContext(), mPlayList);
        } else {
            onStageActionDone(false, false);
        }
    }

    @Override
    public void onStageActionDone(boolean isInterrupted, boolean delayAsrResume) {
        exitScene();
    }
}
