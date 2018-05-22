package com.kikatech.go.services.view.item;

import android.databinding.ViewDataBinding;
import android.graphics.PixelFormat;
import android.media.MediaPlayer;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

import com.kikatech.go.R;
import com.kikatech.go.databinding.YoutubePlayerBinding;
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

    private YoutubePlayerBinding mBinding;


    public ItemYouTubePlayer(View view, View.OnTouchListener listener) {
        super(view, listener);
        setGravity(Gravity.TOP | Gravity.LEFT);
        initPlayer();
    }

    @Override
    protected <T extends ViewDataBinding> void onBindView(T binding) {
        mBinding = (YoutubePlayerBinding) binding;
        bindListener();
    }

    private void bindListener() {
        mBinding.playVideoController.setMediaPlayer(mBinding.playVideoTexture);
        mBinding.playVideoTexture.setPlayerController(mBinding.playVideoController);
        mBinding.playVideoTexture.setVideoStatusListener(mBinding.playVideoController);
    }

    private void initPlayer() {
        switch (mBinding.playVideoController.getPlayerSize()) {
            case SkVideoPlayerView.PlayerSize.MINIMUM:
                mBinding.youtubeBarTitle.setVisibility(View.GONE);
                break;
            case SkVideoPlayerView.PlayerSize.MEDIUM:
            case SkVideoPlayerView.PlayerSize.FULLSCREEN:
                mBinding.youtubeBarTitle.setVisibility(View.VISIBLE);
                break;
        }
    }

    public View getPlayerView() {
        return mBinding.youtubeBarPlayerView;
    }


    public void setControllerVideoCallback(IControllerCallback.IVideoCallback callback) {
        if (mBinding.playVideoController != null) {
            mBinding.playVideoController.setControllerVideoCallback(callback);
        }
    }

    public void setControllerPlayerCallback(IControllerCallback.IPlayerCallback callback) {
        if (mBinding.playVideoController != null) {
            mBinding.playVideoController.setControllerPlayerCallback(callback);
        }
    }

    public void setControllerBehaviorCallback(IControllerCallback.IBehaviorCallback callback) {
        if (mBinding.playVideoController != null) {
            mBinding.playVideoController.setControllerBehaviorCallback(callback);
        }
    }

    public void setControllerStatusCallback(IControllerCallback.IStatusCallback callback) {
        if (mBinding.playVideoController != null) {
            mBinding.playVideoController.setControllerStatusCallback(callback);
        }
    }

    public void setOnPreparedListener(MediaPlayer.OnPreparedListener listener) {
        if (mBinding.playVideoTexture != null) {
            mBinding.playVideoTexture.setOnPreparedListener(listener);
        }
    }

    public void setOnCompletionListener(MediaPlayer.OnCompletionListener listener) {
        if (mBinding.playVideoTexture != null) {
            mBinding.playVideoTexture.setOnCompletionListener(listener);
        }
    }

    public void setOnErrorListener(MediaPlayer.OnErrorListener listener) {
        if (mBinding.playVideoTexture != null) {
            mBinding.playVideoTexture.setOnErrorListener(listener);
        }
    }

    public void setOnSeekCompleteListener(MediaPlayer.OnSeekCompleteListener listener) {
        if (mBinding.playVideoTexture != null) {
            mBinding.playVideoTexture.setOnSeekCompleteListener(listener);
        }
    }

    public void setOnTimeTextListener(MediaPlayer.OnTimedTextListener listener) {
        if (mBinding.playVideoTexture != null) {
            mBinding.playVideoTexture.setOnTimeTextListener(listener);
        }
    }


    public void play(final YouTubeVideo videoToPlay) {
        VideoInfo videoInfo = new VideoInfo.Builder().setPath(videoToPlay.getStreamUrl()).build();
        mBinding.playVideoTexture.setVideo(videoInfo);
        setVideoTitle(videoToPlay.getTitle());
        mBinding.playVideoTexture.start();
        mBinding.playVideoController.updatePausePlay();
    }

    public void pause() {
        mBinding.playVideoTexture.pause();
        mBinding.playVideoController.updatePausePlay();
    }

    public void resume() {
        mBinding.playVideoTexture.resume();
        mBinding.playVideoController.updatePausePlay();
    }

    public void stop() {
        mBinding.playVideoTexture.stop();
        mBinding.playVideoController.updatePausePlay();
    }

    public void volumeUp() {
        mBinding.playVideoTexture.volumeUp();
    }

    public void volumeDown() {
        mBinding.playVideoTexture.volumeDown();
    }

    public void mute() {
        mBinding.playVideoTexture.mute();
    }

    public void unmute() {
        mBinding.playVideoTexture.unmute();
    }


    public void onControllerViewClickEvent(MotionEvent event) {
        if (mBinding.playVideoController != null) {
            mBinding.playVideoController.onClickedEvent(event);
        }
    }


    private void setVideoTitle(final String videoTitle) {
        if (mBinding.youtubeBarTitle != null) {
            mBinding.youtubeBarTitle.setText(videoTitle);
        }
    }


    public void scale(@SkVideoPlayerView.PlayerSize int targetSize) {
        if (mBinding.playVideoController != null) {
            mBinding.playVideoController.setPlayerSize(targetSize);
        }
        mBinding.youtubeBarPlayerView.requestLayout();
        initPlayer();
    }


    @SkVideoPlayerView.PlayerSize
    public int getPlayerSize() {
        return mBinding.playVideoController.getPlayerSize();
    }


    public boolean isPlaying() {
        return mBinding.playVideoTexture != null && mBinding.playVideoTexture.isPlaying();
    }

    public boolean isPrepared() {
        return mBinding.playVideoTexture != null && mBinding.playVideoTexture.isPrepared();
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
