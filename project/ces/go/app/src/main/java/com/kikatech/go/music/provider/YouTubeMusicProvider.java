package com.kikatech.go.music.provider;

import com.kikatech.go.music.model.YouTubeVideo;
import com.kikatech.go.services.MusicForegroundService;
import com.kikatech.go.services.view.item.ItemYouTubePlayer;
import com.kikatech.go.view.youtube.FloatingPlayerController;

/**
 * @author SkeeterWang Created on 2018/1/15.
 */

public class YouTubeMusicProvider implements IMusicProvider<YouTubeVideo> {
    private static final String TAG = "YouTubeMusicProvider";

    private static YouTubeMusicProvider sIns;
    private ItemYouTubePlayer mPlayer;

    public synchronized static YouTubeMusicProvider getIns() {
        if (sIns == null) {
            sIns = new YouTubeMusicProvider();
        }
        return sIns;
    }

    private YouTubeMusicProvider() {
    }

    public void setItemYouTubePlayer(ItemYouTubePlayer player) {
        this.mPlayer = player;
        this.mPlayer.setPlayerStatusCallback(new FloatingPlayerController.IPlayerStatusCallback() {
            @Override
            public void onPlay() {
                MusicForegroundService.processMusicChanged();
            }

            @Override
            public void onPause() {
                MusicForegroundService.processMusicChanged();
            }

            @Override
            public void onBuffer() {
            }
        });
    }

    @Override
    public synchronized void play(YouTubeVideo musicModel) {
        if (mPlayer != null) {
            mPlayer.play(musicModel);
        }
    }

    @Override
    public synchronized void pause() {
        if (mPlayer != null) {
            mPlayer.pause();
        }
    }

    @Override
    public synchronized void resume() {
        if (mPlayer != null) {
            mPlayer.resume();
        }
    }

    @Override
    public void volumeUp() {
        if (mPlayer != null) {
            mPlayer.volumeUp();
        }
    }

    @Override
    public void volumeDown() {
        if (mPlayer != null) {
            mPlayer.volumeDown();
        }
    }

    @Override
    public synchronized void mute() {
        if (mPlayer != null) {
            mPlayer.mute();
        }
    }

    @Override
    public synchronized void unmute() {
        if (mPlayer != null) {
            mPlayer.unmute();
        }
    }

    @Override
    public synchronized void stop() {
        if (mPlayer != null) {
            mPlayer.stop();
        }
    }

    @Override
    public boolean isPlaying() {
        return mPlayer != null && mPlayer.isPlaying();
    }

    @Override
    public boolean isPrepared() {
        return mPlayer != null && mPlayer.isPrepared();
    }
}
