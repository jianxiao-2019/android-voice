package com.kikatech.go.dialogflow.music.stage;

import android.os.Bundle;
import android.support.annotation.NonNull;

import com.kikatech.go.dialogflow.SceneUtil;
import com.kikatech.go.dialogflow.model.TtsText;
import com.kikatech.go.music.model.YouTubeVideo;
import com.kikatech.go.services.MusicForegroundService;
import com.kikatech.voice.core.dialogflow.scene.ISceneFeedback;
import com.kikatech.voice.core.dialogflow.scene.SceneBase;
import com.kikatech.voice.core.dialogflow.scene.SceneStage;

import java.util.ArrayList;

/**
 * @author SkeeterWang Created on 2018/1/17.
 */

class StagePlaySong extends BaseMusicStage {
    private static final String TAG = "StagePlaySong";

    private ArrayList<YouTubeVideo> mPlayList;

    StagePlaySong(@NonNull SceneBase scene, ISceneFeedback feedback, ArrayList<YouTubeVideo> playList) {
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
            String tmp = "OK, playing music.";
            String[] uiAndTtsText = new String[]{tmp, tmp};
            if (uiAndTtsText.length > 0) {
                String uiText = uiAndTtsText[0];
                String ttsText = uiAndTtsText[1];
                TtsText tText = new TtsText(uiText);
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
