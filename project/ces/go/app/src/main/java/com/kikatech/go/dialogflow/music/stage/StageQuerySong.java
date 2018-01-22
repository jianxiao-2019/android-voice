package com.kikatech.go.dialogflow.music.stage;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.kikatech.go.music.google.serivce.YouTubeAPI;
import com.kikatech.go.music.model.YouTubeVideoList;
import com.kikatech.voice.core.dialogflow.scene.ISceneFeedback;
import com.kikatech.voice.core.dialogflow.scene.SceneBase;
import com.kikatech.voice.core.dialogflow.scene.SceneStage;

/**
 * @author SkeeterWang Created on 2018/1/17.
 */

class StageQuerySong extends BaseMusicStage {
    private static final String TAG = "StageQuerySong";

    private String mSongToQuery;
    private YouTubeVideoList mPlayList;

    StageQuerySong(@NonNull SceneBase scene, ISceneFeedback feedback, String songToQuery) {
        super(scene, feedback);
        mSongToQuery = songToQuery;
    }


    @Override
    public SceneStage next(String action, Bundle extra) {
        return super.next(action, extra);
    }

    @Override
    public void doAction() {
        onStageActionStart();
        action();
    }

    @Override
    public void action() {
        if (!TextUtils.isEmpty(mSongToQuery)) {
            YouTubeAPI.getIns().searchVideo(mSongToQuery, new YouTubeAPI.IYoutubeApiCallback() {
                @Override
                public void onLoaded(YouTubeVideoList result) {
                    mPlayList = result;
                    onStageActionDone(false, false);
                }
            });
        } else {
            YouTubeAPI.getIns().searchDefaultRecommendPlayList(new YouTubeAPI.IYoutubeApiCallback() {
                @Override
                public void onLoaded(YouTubeVideoList result) {
                    mPlayList = result;
                    onStageActionDone(false, false);
                }
            });
        }
    }

    @Override
    public void onStageActionDone(boolean isInterrupted, boolean delayAsrResume) {
        if (mPlayList != null && !mPlayList.isEmpty()) {
            SceneStage nextStage = new StagePlaySong(mSceneBase, mFeedback, mPlayList);
            mSceneBase.nextStage(nextStage);
        } else {
            exitScene();
        }
    }
}
