package com.kikatech.go.music;

import com.kikatech.go.music.provider.IMusicProvider;
import com.kikatech.go.music.provider.StreamMusicProvider;

/**
 * @author SkeeterWang Created on 2018/1/5.
 */

public class MusicManager {
    private static final String TAG = "MusicManager";

    private static MusicManager sIns;

    private IMusicProvider mMusicProvider;

    public static synchronized MusicManager getIns() {
        if (sIns == null) {
            sIns = new MusicManager();
        }
        return sIns;
    }

    private MusicManager() {
        mMusicProvider = StreamMusicProvider.getIns();
    }

    public void play() {
        if (mMusicProvider != null) {
            mMusicProvider.play();
        }
    }

    public void pause() {
        if (mMusicProvider != null) {
            mMusicProvider.pause();
        }
    }

    public void resume() {
        if (mMusicProvider != null) {
            mMusicProvider.resume();
        }
    }

    public void mute() {
        if (mMusicProvider != null) {
            mMusicProvider.mute();
        }
    }

    public void unmute() {
        if (mMusicProvider != null) {
            mMusicProvider.unmute();
        }
    }

    public void stop() {
        if (mMusicProvider != null) {
            mMusicProvider.stop();
        }
    }

    public boolean isPlaying() {
        return mMusicProvider != null && mMusicProvider.isPlaying();
    }

    public boolean isPrepared() {
        return mMusicProvider != null && mMusicProvider.isPrepared();
    }
}
