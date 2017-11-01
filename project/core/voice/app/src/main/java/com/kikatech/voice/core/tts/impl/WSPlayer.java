package com.kikatech.voice.core.tts.impl;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.SystemClock;

/**
 * Created by tianli on 17-11-1.
 */

public class WSPlayer {

    private String mUrl;
    private MediaPlayer mMediaPlayer;
    private WSPlayerListener mListener;
    private long mStartTime = 0;
    private long mDuration = 0;

    public WSPlayer(String url) {
        mUrl = url;
    }

    public void play() {
        try {
            mMediaPlayer = createMediaPlayer();
            mMediaPlayer.setDataSource(mUrl);
            mMediaPlayer.setOnPreparedListener(mPreparedListener);
            mMediaPlayer.prepareAsync();
            mMediaPlayer.setOnCompletionListener(mCompletionListener);
            mMediaPlayer.setOnErrorListener(mErrorListener);
        } catch (Exception e) {
            e.printStackTrace();
            if (mListener != null) {
                mListener.onError();
            }
        }
    }

    public void stop() {
        if (mMediaPlayer != null) {
            mMediaPlayer.reset();
        }
    }

    public void setListener(WSPlayerListener listener) {
        mListener = listener;
    }

    private MediaPlayer createMediaPlayer() {
        if (mMediaPlayer != null) {
            mMediaPlayer.reset();
        }
        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        return mMediaPlayer;
    }

    private MediaPlayer.OnPreparedListener mPreparedListener = new MediaPlayer.OnPreparedListener() {
        @Override
        public void onPrepared(MediaPlayer mp) {
            mp.start();
            mStartTime = SystemClock.elapsedRealtime();
            mDuration = mp.getDuration();
            if (mListener != null) {
                mListener.onPrepared();
            }
        }
    };

    private MediaPlayer.OnCompletionListener mCompletionListener = new MediaPlayer.OnCompletionListener() {
        @Override
        public void onCompletion(MediaPlayer mp) {
            if (mListener != null) {
                mListener.onCompletion();
            }
        }
    };

    private MediaPlayer.OnErrorListener mErrorListener = new MediaPlayer.OnErrorListener() {
        @Override
        public boolean onError(MediaPlayer mp, int what, int extra) {
            if (mListener != null) {
                mListener.onError();
            }
            return false;
        }
    };

    public interface WSPlayerListener {
        void onCompletion();

        void onPrepared();

        void onError();
    }
}
