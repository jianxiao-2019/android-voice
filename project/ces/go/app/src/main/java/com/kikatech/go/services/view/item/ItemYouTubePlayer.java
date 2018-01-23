package com.kikatech.go.services.view.item;

import android.graphics.PixelFormat;
import android.media.MediaPlayer;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

import com.kikatech.go.R;
import com.kikatech.go.music.model.YouTubeVideo;
import com.kikatech.go.view.widget.MarqueeTextView;
import com.kikatech.go.view.youtube.model.VideoInfo;
import com.kikatech.go.view.youtube.player.impl.SkVideoPlayerView;
import com.kikatech.go.view.youtube.playercontroller.impl.SkPlayerController;
import com.kikatech.go.view.youtube.playercontroller.impl.SkPlayerController.IControllerCallback;

/**
 * @author SkeeterWang Created on 2018/1/15.
 */

public class ItemYouTubePlayer extends WindowFloatingItem {
    private static final String TAG = "ItemYouTubePlayer";

    private View mPlayerView;
    private SkVideoPlayerView mPlayer;
    private SkPlayerController mPlayerController;
    private MarqueeTextView mVideoTitle;


    public ItemYouTubePlayer(View view, View.OnTouchListener listener) {
        super(view, listener);
        setGravity(Gravity.TOP | Gravity.LEFT);
        initPlayer();
    }

    @Override
    protected void bindView() {
        mPlayerView = mItemView.findViewById(R.id.youtube_bar_player_view);
        mPlayer = (SkVideoPlayerView) mItemView.findViewById(R.id.play_video_texture);
        mPlayerController = (SkPlayerController) mItemView.findViewById(R.id.play_video_controller);
        mVideoTitle = (MarqueeTextView) mItemView.findViewById(R.id.youtube_bar_title);
        bindListener();
    }

    private void bindListener() {
        mPlayerController.setMediaPlayer(mPlayer);
        mPlayer.setPlayerController(mPlayerController);
        mPlayer.setVideoStatusListener(mPlayerController);
    }

    private void initPlayer() {
        switch (mPlayerController.getPlayerSize()) {
            case SkVideoPlayerView.PlayerSize.MINIMUM:
                mVideoTitle.setVisibility(View.GONE);
                break;
            case SkVideoPlayerView.PlayerSize.MEDIUM:
            case SkVideoPlayerView.PlayerSize.FULLSCREEN:
                mVideoTitle.setVisibility(View.VISIBLE);
                break;
        }
    }

    public View getPlayerView() {
        return mPlayerView;
    }


    public void setControllerVideoCallback(IControllerCallback.IVideoCallback callback) {
        if (mPlayerController != null) {
            mPlayerController.setControllerVideoCallback(callback);
        }
    }

    public void setControllerPlayerCallback(IControllerCallback.IPlayerCallback callback) {
        if (mPlayerController != null) {
            mPlayerController.setControllerPlayerCallback(callback);
        }
    }

    public void setControllerBehaviorCallback(IControllerCallback.IBehaviorCallback callback) {
        if (mPlayerController != null) {
            mPlayerController.setControllerBehaviorCallback(callback);
        }
    }

    public void setControllerStatusCallback(IControllerCallback.IStatusCallback callback) {
        if (mPlayerController != null) {
            mPlayerController.setControllerStatusCallback(callback);
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
        VideoInfo videoInfo = new VideoInfo.Builder().setPath(videoToPlay.getStreamUrl()).build();
        mPlayer.setVideo(videoInfo);
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
        mPlayer.stop();
        mPlayerController.updatePausePlay();
    }

    public void volumeUp() {
        mPlayer.volumeUp();
    }

    public void volumeDown() {
        mPlayer.volumeDown();
    }

    public void mute() {
        mPlayer.mute();
    }

    public void unmute() {
        mPlayer.unmute();
    }


    public void onControllerViewClickEvent(MotionEvent event) {
        if (mPlayerController != null) {
            mPlayerController.onClickedEvent(event);
        }
    }


    private void setVideoTitle(final String videoTitle) {
        if (mVideoTitle != null) {
            mVideoTitle.setText(videoTitle);
        }
    }


    public void scale(@SkVideoPlayerView.PlayerSize int targetSize) {
        if (mPlayerController != null) {
            mPlayerController.setPlayerSize(targetSize);
        }
        mPlayerView.requestLayout();
        initPlayer();
    }


    @SkVideoPlayerView.PlayerSize
    public int getPlayerSize() {
        return mPlayerController.getPlayerSize();
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
