package com.kikatech.go.services.view.item;

import android.graphics.PixelFormat;
import android.media.MediaPlayer;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;

import com.kikatech.go.R;
import com.kikatech.go.music.model.YouTubeVideo;
import com.kikatech.go.view.widget.MarqueeTextView;
import com.kikatech.go.view.youtube.FensterVideoView;
import com.kikatech.go.view.youtube.FloatingPlayerController;

/**
 * @author SkeeterWang Created on 2018/1/15.
 */

public class ItemYouTubePlayer extends WindowFloatingItem {
    private static final String TAG = "ItemYouTubePlayer";

    @FensterVideoView.PlayerSize
    private int mPlayerSize = FensterVideoView.PlayerSize.DEFAULT;

    private View mPlayerView;
    private FensterVideoView mPlayer;
    private FloatingPlayerController mPlayerController;
    private MarqueeTextView mVideoTitle;

    public ItemYouTubePlayer(View view, View.OnTouchListener listener) {
        super(view, listener);
        setGravity(Gravity.TOP | Gravity.LEFT);
        initPlayer();
    }

    @Override
    protected void bindView() {
        mPlayerView = mItemView.findViewById(R.id.youtube_bar_player_view);
        mPlayer = (FensterVideoView) mItemView.findViewById(R.id.play_video_texture);
        mPlayerController = (FloatingPlayerController) mItemView.findViewById(R.id.play_video_controller);
        mVideoTitle = (MarqueeTextView) mItemView.findViewById(R.id.youtube_bar_title);
        bindListener();
    }

    private void bindListener() {
        mPlayerController.setMediaPlayer(mPlayer);
        mPlayer.setMediaController(mPlayerController);
        mPlayer.setOnPlayStateListener(mPlayerController);
    }

    private void initPlayer() {
        switch (mPlayerSize) {
            case FensterVideoView.PlayerSize.MINIMUM:
                mVideoTitle.setVisibility(View.GONE);
                break;
            case FensterVideoView.PlayerSize.MEDIUM:
            case FensterVideoView.PlayerSize.FULLSCREEN:
                mVideoTitle.setVisibility(View.VISIBLE);
                break;
        }
    }

    public View getPlayerView() {
        return mPlayerView;
    }


    public void setControllerCallback(FloatingPlayerController.IControllerCallback callback) {
        if (mPlayerController != null) {
            mPlayerController.setControllerListener(callback);
        }
    }

    public void setPlayerStatusCallback(FloatingPlayerController.IPlayerStatusCallback callback) {
        if (mPlayerController != null) {
            mPlayerController.setPlayerStatusCallback(callback);
        }
    }

    public void setOnPreparedListener(MediaPlayer.OnPreparedListener listener) {
        if (mPlayer != null) {
            mPlayer.setOnPreparedListener(listener);
        }
    }

    public void setOnCompletionListener(MediaPlayer.OnCompletionListener listener) {
        if (mPlayer != null) {
            mPlayer.setOnCompletionListener(listener);
        }
    }

    public void setOnErrorListener(MediaPlayer.OnErrorListener listener) {
        if (mPlayer != null) {
            mPlayer.setOnErrorListener(listener);
        }
    }

    public void setOnSeekCompleteListener(MediaPlayer.OnSeekCompleteListener listener) {
        if (mPlayer != null) {
            mPlayer.setOnSeekCompleteListener(listener);
        }
    }

    public void setOnTimeTextListener(MediaPlayer.OnTimedTextListener listener) {
        if (mPlayer != null) {
            mPlayer.setOnTimeTextListener(listener);
        }
    }


    public void play(final YouTubeVideo videoToPlay) {
        mPlayer.setVideo(videoToPlay.getStreamUrl());
        setVideoTitle(videoToPlay.getTitle());
        mPlayer.start();
        mPlayerController.updatePausePlay();
    }

    public void pause() {
        mPlayer.pause();
        mPlayerController.updatePausePlay();
    }

    public void resume() {
        mPlayer.start();
        mPlayerController.updatePausePlay();
    }

    public void stop() {
        mPlayer.stopPlayback();
        mPlayerController.updatePausePlay();
    }

    public void performControllerView(float rawX, float rawY) {
        if (mPlayerController != null) {
            mPlayerController.performControllerView(rawX, rawY);
        }
    }


    private void setVideoTitle(final String videoTitle) {
        if (mVideoTitle != null) {
            mVideoTitle.setText(videoTitle);
        }
    }


    @FensterVideoView.PlayerSize
    public int getPlayerSize() {
        return mPlayerSize;
    }


    public boolean isPlaying() {
        return mPlayer != null && mPlayer.isPlaying();
    }

    public boolean isPrepared() {
        return mPlayer != null && mPlayer.isPrepared();
    }


    @Override
    protected WindowManager.LayoutParams getDefaultParam() {
        return new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_SYSTEM_ERROR,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                        WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED |
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON,
                PixelFormat.TRANSLUCENT
        );
    }
}
