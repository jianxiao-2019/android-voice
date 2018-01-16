package com.kikatech.go.music.provider;

import android.media.AudioManager;
import android.media.MediaPlayer;

import com.kikatech.go.services.MusicForegroundService;
import com.kikatech.go.util.LogUtil;
import com.kikatech.go.util.MediaPlayerUtil;

/**
 * @author SkeeterWang Created on 2018/1/5.
 */

public class StreamMusicProvider implements IMusicProvider {
    private static final String TAG = "StreamMusicProvider";

    private static final String STREAM_LINK = "http://178.63.62.145:8080/wide01";
    private static final String STREAM_LINK2 = "http://78.46.246.97:9000";

    private static StreamMusicProvider sIns;

    private boolean isPrepared = false;

    private MediaPlayer mMediaPlayer;
    private final MediaPlayer.OnPreparedListener onPreparedListener = new MediaPlayer.OnPreparedListener() {
        @Override
        public void onPrepared(MediaPlayer mp) {
            isPrepared = true;
            if (!mMediaPlayer.isPlaying()) {
                mMediaPlayer.start();
            }
            MusicForegroundService.processMusicChanged();
            if (LogUtil.DEBUG) {
                LogUtil.logd(TAG, ">> Player Prepared");
            }
        }
    };

    public synchronized static StreamMusicProvider getIns() {
        if (sIns == null) {
            sIns = new StreamMusicProvider();
        }
        return sIns;
    }

    private StreamMusicProvider() {
        mMediaPlayer = new MediaPlayer();
    }

    @Override
    public void play() {
        reset();
        try {
            MediaPlayerUtil.safeSetMediaPlayerSource(mMediaPlayer, STREAM_LINK);
            mMediaPlayer.setOnPreparedListener(onPreparedListener);
            //mediaPlayer.prepare(); // might take long! (for buffering, etc)   //@@
            mMediaPlayer.prepareAsync();
            MusicForegroundService.processMusicChanged();
            if (LogUtil.DEBUG) {
                LogUtil.logd(TAG, ">> Player Play");
            }
        } catch (Exception e) {
            if (LogUtil.DEBUG) {
                LogUtil.printStackTrace(TAG, e.getMessage(), e);
            }
        }
    }

    @Override
    public void pause() {
        try {
            mMediaPlayer.pause();
            MusicForegroundService.processMusicChanged();
            if (LogUtil.DEBUG) {
                LogUtil.logd(TAG, ">> Player Pause");
            }
        } catch (Exception e) {
            if (LogUtil.DEBUG) {
                LogUtil.printStackTrace(TAG, e.getMessage(), e);
            }
        }
    }

    @Override
    public void resume() {
        try {
            mMediaPlayer.start();
            MusicForegroundService.processMusicChanged();
            if (LogUtil.DEBUG) {
                LogUtil.logd(TAG, ">> Player Resume");
            }
        } catch (Exception e) {
            if (LogUtil.DEBUG) {
                LogUtil.printStackTrace(TAG, e.getMessage(), e);
            }
        }
    }

    @Override
    public void mute() {
        try {
            mMediaPlayer.setVolume(0, 0);
        } catch (Exception e) {
            if (LogUtil.DEBUG) {
                LogUtil.printStackTrace(TAG, e.getMessage(), e);
            }
        }
    }

    @Override
    public void unmute() {
        try {
            mMediaPlayer.setVolume(1, 1);
        } catch (Exception e) {
            if (LogUtil.DEBUG) {
                LogUtil.printStackTrace(TAG, e.getMessage(), e);
            }
        }
    }

    @Override
    public void stop() {
        try {
            isPrepared = false;
            mMediaPlayer.stop();
            mMediaPlayer.reset();
            if (LogUtil.DEBUG) {
                LogUtil.logd(TAG, ">> Player Stop");
            }
        } catch (Exception e) {
            if (LogUtil.DEBUG) {
                LogUtil.printStackTrace(TAG, e.getMessage(), e);
            }
        }
    }

    @Override
    public boolean isPlaying() {
        try {
            if (mMediaPlayer != null) {
                return mMediaPlayer.isPlaying();
            }
        } catch (Exception e) {
            if (LogUtil.DEBUG) {
                LogUtil.printStackTrace(TAG, e.getMessage(), e);
            }
        }
        return false;
    }

    @Override
    public boolean isPrepared() {
        return isPrepared;
    }

    private void reset() {
        isPrepared = false;
        mMediaPlayer.reset();
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mMediaPlayer.setOnPreparedListener(null);
    }
}