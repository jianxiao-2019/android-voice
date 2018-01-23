package com.kikatech.go.view.youtube.player.impl;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.TypedArray;
import android.graphics.SurfaceTexture;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.TimedText;
import android.net.Uri;
import android.support.annotation.IntDef;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.TextureView;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.MediaController;

import com.kikatech.go.R;
import com.kikatech.go.util.LogUtil;
import com.kikatech.go.util.TimeUtil;
import com.kikatech.go.view.youtube.model.VideoInfo;
import com.kikatech.go.view.youtube.player.interfaces.IVideoPlayer;
import com.kikatech.go.view.youtube.player.interfaces.IVideoStatusListener;
import com.kikatech.go.view.youtube.playercontroller.interfaces.IVideoPlayerController;

import java.io.IOException;

/**
 * @author SkeeterWang Created on 2018/1/18.
 */

public class SkVideoPlayerView extends TextureView implements IVideoPlayer, MediaController.MediaPlayerControl,
        MediaPlayer.OnPreparedListener,
        MediaPlayer.OnCompletionListener,
        MediaPlayer.OnErrorListener,
        MediaPlayer.OnInfoListener,
        MediaPlayer.OnSeekCompleteListener,
        MediaPlayer.OnTimedTextListener,
        MediaPlayer.OnBufferingUpdateListener,
        MediaPlayer.OnVideoSizeChangedListener {

    private static final String TAG = "SkVideoPlayer";

    private static final float VOLUME_SCALE_INTERVAL = 0.3f;
    // --------------------------------------------------
    private static final int SCALE_TYPE_FIT = 0;
    private static final int SCALE_TYPE_CROP = 1;
    // --------------------------------------------------
    private static final int PLAYER_SIZE_FULLSCREEN = 0;
    private static final int PLAYER_SIZE_MEDIUM = 1;
    private static final int PLAYER_SIZE_MINIMUM = 2;
    // --------------------------------------------------
    /* all possible internal states */
    private static final int STATE_ERROR = -1;
    private static final int STATE_IDLE = 0;
    private static final int STATE_PREPARING = 1;
    private static final int STATE_PREPARED = 2;
    private static final int STATE_PLAYING = 3;
    private static final int STATE_PAUSED = 4;
    private static final int STATE_PLAYBACK_COMPLETED = 5;
    // --------------------------------------------------


    @IntDef({SCALE_TYPE_FIT, SCALE_TYPE_CROP})
    private @interface ScaleType {
        int DEFAULT = SCALE_TYPE_FIT;
        int FIT = SCALE_TYPE_FIT;
        int CROP = SCALE_TYPE_CROP;
    }

    @IntDef({PLAYER_SIZE_FULLSCREEN, PLAYER_SIZE_MEDIUM, PLAYER_SIZE_MINIMUM})
    public @interface PlayerSize {
        int DEFAULT = PLAYER_SIZE_MINIMUM;
        int MINIMUM = PLAYER_SIZE_MINIMUM;
        int MEDIUM = PLAYER_SIZE_MEDIUM;
        int FULLSCREEN = PLAYER_SIZE_FULLSCREEN;
    }

    @IntDef({STATE_ERROR, STATE_IDLE, STATE_PREPARING, STATE_PREPARED, STATE_PLAYING, STATE_PAUSED, STATE_PLAYBACK_COMPLETED})
    private @interface PlayerState {
        int DEFAULT = STATE_IDLE;
        int ERROR = STATE_ERROR;
        int IDLE = STATE_IDLE;
        int PREPARING = STATE_PREPARING;
        int PREPARED = STATE_PREPARED;
        int PLAYING = STATE_PLAYING;
        int PAUSED = STATE_PAUSED;
        int PLAYBACK_COMPLETED = STATE_PLAYBACK_COMPLETED;
    }

    // --------------------------------------------------

    @ScaleType
    private int mScaleType;
    @PlayerSize
    private int mPlayerSize;

    /*
     * mCurrentState is a VideoView object's current state.
     * mTargetState is the state that a method caller intends to reach.
     *
     * For instance, regardless the VideoView object's current state,
     * calling pause() intends to bring the object to a target state of STATE_PAUSED.
     */
    @PlayerState
    private int mCurrentState = PlayerState.IDLE;
    @PlayerState
    private int mTargetState = PlayerState.IDLE;


    private MediaPlayer mMediaPlayer;
    // --------------------------------------------------
    private MediaPlayer.OnPreparedListener mOnPreparedListener;
    private MediaPlayer.OnCompletionListener mOnCompletionListener;
    private MediaPlayer.OnErrorListener mOnErrorListener;
    private MediaPlayer.OnInfoListener mOnInfoListener;
    private MediaPlayer.OnSeekCompleteListener mOnSeekCompleteListener;
    private MediaPlayer.OnTimedTextListener mOnTimeTextListener;
    private MediaPlayer.OnBufferingUpdateListener mOnBufferingUpdateListener;
    private MediaPlayer.OnVideoSizeChangedListener mOnVideoSizeChangedListener;
    // --------------------------------------------------


    private SurfaceTexture mSurfaceTexture;
    private SurfaceTextureListener mSurfaceTextureListener = new SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            if (LogUtil.DEBUG) {
                LogUtil.log(TAG, String.format("onSurfaceTextureAvailable, width: %1$s, height: %2$s", width, height));
            }
            mSurfaceTexture = surface;
//            openVideo();
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
            if (LogUtil.DEBUG) {
                LogUtil.log(TAG, String.format("onSurfaceTextureSizeChanged, width: %1$s, height: %2$s", width, height));
            }
            boolean isValidState = (mTargetState == PlayerState.PLAYING);
            boolean hasValidSize = mVideoSizeCalculator.isTargetSizeEqual(width, height);
            if (mMediaPlayer != null && isValidState && hasValidSize) {
                if (mSeekWhenPrepared != 0) {
                    seekTo(mSeekWhenPrepared);
                }
                start();
            }
        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            if (LogUtil.DEBUG) {
                LogUtil.log(TAG, "onSurfaceTextureDestroyed");
            }
            mSurfaceTexture = null;
            hidePlayerController();
            release(true);
            return false;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {
            mSurfaceTexture = surface;
        }
    };
    // --------------------------------------------------

    // collaborators / delegates / composites .. discuss
    private final VideoSizeCalculator mVideoSizeCalculator = new VideoSizeCalculator();
    // --------------------------------------------------

    private IVideoPlayerController mPlayerController;
    private IVideoStatusListener mVideoStatusListener;
    // --------------------------------------------------


    // ---------- Player Information ----------
    private int mAudioSession;
    private int mSeekWhenPrepared;  // recording the seek position while preparing
    private int mCurrentBufferPercentage;
    private float mVolumeScalar = 1.0f;


    // ---------- Video Information ----------
    private VideoInfo mVideoInfo;


    private boolean mCanPause;
    private boolean mCanSeekBackward;
    private boolean mCanSeekForward;


    // ---------- Constructors ----------

    public SkVideoPlayerView(Context context) {
        this(context, null);
    }

    public SkVideoPlayerView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SkVideoPlayerView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public SkVideoPlayerView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs);
        initVideoView();
    }

    private void init(Context context, AttributeSet attrs) {
        if (attrs != null) {
            TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.SkVideoPlayerView);
            mScaleType = typedArray.getInt(R.styleable.SkVideoPlayerView_scaleType, ScaleType.DEFAULT);
            typedArray.recycle();
        }
    }

    private void initVideoView() {
        mVideoSizeCalculator.setTargetVideoSize(0, 0);
        setSurfaceTextureListener(mSurfaceTextureListener);
        setFocusable(true);
        setFocusableInTouchMode(true);
        requestFocus();
        mCurrentState = PlayerState.DEFAULT;
        mTargetState = PlayerState.DEFAULT;
        setOnInfoListener(new MediaPlayer.OnInfoListener() {
            @Override
            public boolean onInfo(MediaPlayer mp, int what, int extra) {
                if (mVideoStatusListener != null) {
                    switch (what) {
                        case MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START:
                            mVideoStatusListener.onFirstVideoFrameRendered();
                            mVideoStatusListener.onPlay();
                            break;
                        case MediaPlayer.MEDIA_INFO_BUFFERING_START:
                            mVideoStatusListener.onBuffer();
                            break;
                        case MediaPlayer.MEDIA_INFO_BUFFERING_END:
                            mVideoStatusListener.onPlay();
                            break;
                    }
                }
                return false;
            }
        });
    }


    // ---------- View functions override ----------

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        VideoSizeCalculator.Dimens dimens = mVideoSizeCalculator.measure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(dimens.getWidth(), dimens.getHeight());
    }

    @Override
    public void onInitializeAccessibilityEvent(AccessibilityEvent event) {
        super.onInitializeAccessibilityEvent(event);
        event.setClassName(SkVideoPlayerView.class.getName());
    }

    @Override
    public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo info) {
        super.onInitializeAccessibilityNodeInfo(info);
        info.setClassName(SkVideoPlayerView.class.getName());
    }

    @Override
    public boolean onTrackballEvent(MotionEvent event) {
        if (LogUtil.DEBUG) {
            LogUtil.log(TAG, "onTrackballEvent, ev: " + event.toString());
        }
        if (isInPlaybackState()) {
            showPlayerController();
        }
        return false;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        boolean isKeyCodeSupported = keyCode != KeyEvent.KEYCODE_BACK &&
                keyCode != KeyEvent.KEYCODE_VOLUME_UP &&
                keyCode != KeyEvent.KEYCODE_VOLUME_DOWN &&
                keyCode != KeyEvent.KEYCODE_VOLUME_MUTE &&
                keyCode != KeyEvent.KEYCODE_MENU &&
                keyCode != KeyEvent.KEYCODE_CALL &&
                keyCode != KeyEvent.KEYCODE_ENDCALL;

        if (isInPlaybackState() && isKeyCodeSupported && mPlayerController != null) {
            switch (keyCode) {
                case KeyEvent.KEYCODE_HEADSETHOOK:
                case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
                    if (mMediaPlayer.isPlaying()) {
                        pause();
                        showPlayerController();
                    } else {
                        start();
                        hidePlayerController();
                    }
                    return true;
                case KeyEvent.KEYCODE_MEDIA_PLAY:
                    if (!mMediaPlayer.isPlaying()) {
                        start();
                        hidePlayerController();
                    }
                    return true;
                case KeyEvent.KEYCODE_MEDIA_STOP:
                case KeyEvent.KEYCODE_MEDIA_PAUSE:
                    if (mMediaPlayer.isPlaying()) {
                        pause();
                        showPlayerController();
                    }
                    return true;
                default:
                    showPlayerController();
                    break;
            }
        }

        return super.onKeyDown(keyCode, event);
    }


    // ---------- Customized MediaPlayer Callbacks Setters ----------

    public void setOnPreparedListener(MediaPlayer.OnPreparedListener listener) {
        this.mOnPreparedListener = listener;
    }

    public void setOnCompletionListener(MediaPlayer.OnCompletionListener listener) {
        this.mOnCompletionListener = listener;
    }

    public void setOnErrorListener(MediaPlayer.OnErrorListener listener) {
        this.mOnErrorListener = listener;
    }

    public void setOnInfoListener(MediaPlayer.OnInfoListener listener) {
        this.mOnInfoListener = listener;
    }

    public void setOnSeekCompleteListener(MediaPlayer.OnSeekCompleteListener listener) {
        this.mOnSeekCompleteListener = listener;
    }

    public void setOnTimeTextListener(MediaPlayer.OnTimedTextListener listener) {
        this.mOnTimeTextListener = listener;
    }

    public void setOnBufferingUpdateListener(MediaPlayer.OnBufferingUpdateListener listener) {
        this.mOnBufferingUpdateListener = listener;
    }

    public void setOnVideoSizeChangedListener(MediaPlayer.OnVideoSizeChangedListener listener) {
        this.mOnVideoSizeChangedListener = listener;
    }


    // ---------- MediaPlayer Callbacks Dispatcher Impl ----------

    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        performOnPrepared(mediaPlayer);
    }

    private void performOnPrepared(MediaPlayer mediaPlayer) {
        if (LogUtil.DEBUG) {
            LogUtil.log(TAG, "performOnPrepared, mTargetState: " + mTargetState);
        }
        mCurrentState = PlayerState.PREPARED;

        mCanPause = true;
        mCanSeekBackward = true;
        mCanSeekForward = true;

        enablePlayerController();

        mVideoSizeCalculator.setTargetVideoSize(mediaPlayer.getVideoWidth(), mediaPlayer.getVideoHeight());

        int seekToPosition = mSeekWhenPrepared;  // mSeekWhenPrepared may be changed after seekTo() call

        if (seekToPosition != 0) {
            seekTo(seekToPosition);
        }

        if (mTargetState == PlayerState.PLAYING) {
            start();
        } else if (pausedAt(seekToPosition)) {
            showStickyPlayerController();
        }

        if (mOnPreparedListener != null) {
            mOnPreparedListener.onPrepared(mMediaPlayer);
        }
    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        performOnComplete(mediaPlayer);
    }

    private void performOnComplete(MediaPlayer mediaPlayer) {
        setKeepScreenOn(false);
        mCurrentState = PlayerState.PLAYBACK_COMPLETED;
        mTargetState = PlayerState.PLAYBACK_COMPLETED;
        hidePlayerController();
        if (mOnCompletionListener != null) {
            mOnCompletionListener.onCompletion(mMediaPlayer);
        }
    }

    @Override
    public boolean onError(MediaPlayer mediaPlayer, int frameworkError, int implError) {
        return performOnError(mediaPlayer, frameworkError, implError);
    }

    private boolean performOnError(MediaPlayer mediaPlayer, int frameworkError, int implError) {
        if (LogUtil.DEBUG) {
            LogUtil.logw(TAG, String.format("frameworkError: %1$s, implError: %2$s", frameworkError, implError));
        }

        if (PlayerState.ERROR == mCurrentState) {
            return true;
        }

        mCurrentState = PlayerState.ERROR;
        mTargetState = PlayerState.ERROR;

        hidePlayerController();

        boolean isExternalError = MediaPlayer.MEDIA_ERROR_IO == frameworkError || MediaPlayer.MEDIA_ERROR_UNKNOWN == frameworkError;
        if (mVideoStatusListener != null && isExternalError) { // let VideoStatusListener handle the external errors
            return mVideoStatusListener.onStopWithExternalError(getCurrentPosition() / TimeUtil.MILLIS_IN_SECOND);
        }

        if (mOnErrorListener != null) { // let customized listener handle the error
            return mOnErrorListener.onError(mMediaPlayer, frameworkError, implError);
        }

        if (LogUtil.DEBUG) {
            LogUtil.logw(TAG, String.format("TextureVideoView error. %1$s", getErrorMessage(frameworkError)));
        }

        return true;
    }

    private static String getErrorMessage(final int frameworkError) {
        String message = "Impossible to play the video.";
        switch (frameworkError) {
            case MediaPlayer.MEDIA_ERROR_IO:
                message = "File or network related operation errors.";
                break;
            case MediaPlayer.MEDIA_ERROR_MALFORMED:
                message = "Bit stream is not conforming to the related coding standard or file spec.";
                break;
            case MediaPlayer.MEDIA_ERROR_SERVER_DIED:
                message = "Media server died. In this case, the application must release the MediaPlayer object and instantiate a new one.";
                break;
            case MediaPlayer.MEDIA_ERROR_TIMED_OUT:
                message = "Some operation takes too long to complete, usually more than 3-5 seconds.";
                break;
            case MediaPlayer.MEDIA_ERROR_UNKNOWN:
                message = "Unspecified media player error.";
                break;
            case MediaPlayer.MEDIA_ERROR_UNSUPPORTED:
                message = "Bitstream is conforming to the related coding standard or file spec, but the media framework does not support the feature.";
                break;
            case MediaPlayer.MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK:
                message = "Impossible to play the progressive video. The video is streamed and its container is not valid for progressive playback i.e the video's index (e.g moov atom) is not at the start of the file.";
                break;
        }
        return message;
    }

    @Override
    public boolean onInfo(MediaPlayer mediaPlayer, int what, int extra) {
        return performOnInfo(mediaPlayer, what, extra);
    }

    private boolean performOnInfo(MediaPlayer mediaPlayer, int what, int extra) {
        if (mOnInfoListener != null) { // let customized listener handle the info
            mOnInfoListener.onInfo(mMediaPlayer, what, extra);
        }
        return true;
    }

    @Override
    public void onSeekComplete(MediaPlayer mediaPlayer) {
        performOnSeekComplete(mediaPlayer);
    }

    private void performOnSeekComplete(MediaPlayer mediaPlayer) {
        if (mOnSeekCompleteListener != null) {
            mOnSeekCompleteListener.onSeekComplete(mediaPlayer);
        }
    }

    @Override
    public void onTimedText(MediaPlayer mediaPlayer, TimedText timedText) {
        performOnTimedText(mediaPlayer, timedText);
    }

    private void performOnTimedText(MediaPlayer mediaPlayer, TimedText timedText) {
        if (mOnTimeTextListener != null) {
            mOnTimeTextListener.onTimedText(mediaPlayer, timedText);
        }
    }

    @Override
    public void onBufferingUpdate(MediaPlayer mediaPlayer, int percent) {
        performBufferingUpdate(mediaPlayer, percent);
    }

    private void performBufferingUpdate(MediaPlayer mediaPlayer, int percent) {
        mCurrentBufferPercentage = percent;
        if (mOnBufferingUpdateListener != null) {
            mOnBufferingUpdateListener.onBufferingUpdate(mediaPlayer, percent);
        }
    }

    @Override
    public void onVideoSizeChanged(MediaPlayer mediaPlayer, int width, int height) {
        performOnVideoSizeChanged(mediaPlayer, width, height);
    }

    private void performOnVideoSizeChanged(MediaPlayer mediaPlayer, int width, int height) {
        mVideoSizeCalculator.setTargetVideoSize(mediaPlayer.getVideoWidth(), mediaPlayer.getVideoHeight());
        if (mVideoSizeCalculator.isTargetSizeValid()) {
            requestLayout();
        }
        if (mOnVideoSizeChangedListener != null) {
            mOnVideoSizeChangedListener.onVideoSizeChanged(mediaPlayer, width, height);
        }
    }


    // ---------- IVideoPlayer Callbacks Impl ----------

    @Override
    public void start() {
        if (isInPlaybackState()) {
            mMediaPlayer.start();
            setKeepScreenOn(true);
            mCurrentState = PlayerState.PLAYING;
        }
        mTargetState = PlayerState.PLAYING;
    }

    @Override
    public void pause() {
        if (isInPlaybackState()) {
            if (mMediaPlayer.isPlaying()) {
                mMediaPlayer.pause();
                mCurrentState = PlayerState.PAUSED;
                setKeepScreenOn(false);
                if (mVideoStatusListener != null) {
                    mVideoStatusListener.onPause();
                }
            }
        }
        mTargetState = PlayerState.PAUSED;
    }

    @Override
    public void resume() {
        openVideo();
    }

    @Override
    public void stop() {
        if (mMediaPlayer != null) {
            mMediaPlayer.stop();
            mMediaPlayer.release();
            mMediaPlayer = null;
            setKeepScreenOn(false);
            mCurrentState = PlayerState.IDLE;
            mTargetState = PlayerState.IDLE;
        }
    }

    @Override
    public void release(boolean clearTargetState) {
        if (mMediaPlayer != null) {
            mMediaPlayer.reset();
            mMediaPlayer.release();
            mMediaPlayer = null;
            mCurrentState = PlayerState.IDLE;
            if (clearTargetState) {
                mTargetState = PlayerState.IDLE;
            }
        }
    }

    @Override
    public void seekTo(int millis) {
        if (isInPlaybackState()) {
            mMediaPlayer.seekTo(millis);
            mSeekWhenPrepared = 0;
        } else {
            mSeekWhenPrepared = millis;
        }
    }

    @Override
    public int getDuration() {
        if (isInPlaybackState()) {
            return mMediaPlayer.getDuration();
        }
        return -1;
    }

    @Override
    public int getCurrentPosition() {
        if (mMediaPlayer != null && PlayerState.PLAYBACK_COMPLETED == mCurrentState) {
            return mMediaPlayer.getDuration();
        } else if (mMediaPlayer != null && isInPlaybackState()) {
            return mMediaPlayer.getCurrentPosition();
        }
        return 0;
    }

    @Override
    public int getBufferPercentage() {
        if (mMediaPlayer != null) {
            return mCurrentBufferPercentage;
        }
        return 0;
    }

    @Override
    public boolean isPlaying() {
        return isInPlaybackState() && mMediaPlayer.isPlaying();
    }

    @Override
    public boolean isPrepared() {
        return isInPlaybackState() && PlayerState.PREPARED == mCurrentState;
    }


    // ---------- MediaPlayerControl Callbacks ----------

    @Override
    public boolean canPause() {
        return mCanPause;
    }

    @Override
    public boolean canSeekBackward() {
        return mCanSeekBackward;
    }

    @Override
    public boolean canSeekForward() {
        return mCanSeekForward;
    }

    @Override
    public int getAudioSessionId() {
        if (mAudioSession == 0) {
            MediaPlayer foo = new MediaPlayer();
            mAudioSession = foo.getAudioSessionId();
            foo.release();
        }
        return mAudioSession;
    }


    // ---------- Player ----------

    public void setVideo(final VideoInfo videoInfo) {
        if (LogUtil.DEBUG) {
            if (videoInfo != null) {
                videoInfo.print(TAG);
            } else {
                LogUtil.logw(TAG, "videoInfo is null");
            }
        }
        mVideoInfo = videoInfo;
        mSeekWhenPrepared = mVideoInfo != null ? mVideoInfo.getSeekInSeconds() * TimeUtil.MILLIS_IN_SECOND : 0;
        openVideo();
        requestLayout();
        invalidate();
    }

    private void openVideo() {
        if (mSurfaceTexture == null) {
            if (LogUtil.DEBUG) {
                LogUtil.logw(TAG, "Not ready for playback just yet, will try again later.");
            }
            return;
        }

        if (mVideoInfo == null) {
            if (LogUtil.DEBUG) {
                LogUtil.logw(TAG, "Video info is null.");
            }
            return;
        }

        // we shouldn't clear the target state, because somebody might have called start() previously
        release(false);

        try {
            mMediaPlayer = new MediaPlayer();

            mCurrentBufferPercentage = 0;

            if (mAudioSession != 0) {
                mMediaPlayer.setAudioSessionId(mAudioSession);
            } else {
                mAudioSession = mMediaPlayer.getAudioSessionId();
            }

            mMediaPlayer.setOnPreparedListener(this);
            mMediaPlayer.setOnCompletionListener(this);
            mMediaPlayer.setOnErrorListener(this);
            mMediaPlayer.setOnInfoListener(this);
            mMediaPlayer.setOnSeekCompleteListener(this);
            mMediaPlayer.setOnBufferingUpdateListener(this);
            mMediaPlayer.setOnVideoSizeChangedListener(this);

            setCaption(mVideoInfo.getCaptionFilePath());
            setDataSource(mVideoInfo);
            setScaleType(mScaleType);

            mMediaPlayer.setSurface(new Surface(mSurfaceTexture));
            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mMediaPlayer.setScreenOnWhilePlaying(true);
            mMediaPlayer.prepareAsync();

            // we don't set the target state here either, but preserve the target state that was there before.
            mCurrentState = PlayerState.PREPARING;
            attachPlayerController();

            // work around due to MediaPlayer.MEDIA_INFO_BUFFERING_START might not work on some devices
            showPlayerControllerLoading();
        } catch (Exception e) {
            if (LogUtil.DEBUG) {
                LogUtil.printStackTrace(TAG, e.getMessage(), e);
            }
            notifyUnableToOpenContent();
        }
    }

    private void setCaption(String captionFilePath) {
        if (mMediaPlayer == null || TextUtils.isEmpty(captionFilePath)) {
            return;
        }
        try {
            mMediaPlayer.setOnTimedTextListener(this);
            mMediaPlayer.addTimedTextSource(captionFilePath, MediaPlayer.MEDIA_MIMETYPE_TEXT_SUBRIP);
            int textTrackIndex = findCaptionTrackIndexFor(MediaPlayer.TrackInfo.MEDIA_TRACK_TYPE_TIMEDTEXT, mMediaPlayer.getTrackInfo());
            if (textTrackIndex >= 0) {
                mMediaPlayer.selectTrack(textTrackIndex);
            } else {
                if (LogUtil.DEBUG) {
                    LogUtil.logw(TAG, "Cannot find text track!");
                }
            }
        } catch (Exception e) {
            // We wouldn't like to interrupt video playback just 'cause of caption issue
            if (LogUtil.DEBUG) {
                LogUtil.printStackTrace(TAG, e.getMessage(), e);
            }
        }
    }

    private int findCaptionTrackIndexFor(int mediaTrackType, MediaPlayer.TrackInfo[] trackInfo) {
        int index = -1;
        for (int i = 0; i < trackInfo.length; i++) {
            if (trackInfo[i].getTrackType() == mediaTrackType) {
                return i;
            }
        }
        return index;
    }

    private void setDataSource(VideoInfo videoInfo) throws IOException {
        if (videoInfo == null || mMediaPlayer == null) {
            return;
        }
        AssetFileDescriptor afd = videoInfo.getAssetFileDescriptor();
        Uri uri = videoInfo.getUri();
        if (afd != null) {
            mMediaPlayer.setDataSource(
                    afd.getFileDescriptor(),
                    afd.getStartOffset(),
                    afd.getLength()
            );
        } else if (uri != null) {
            mMediaPlayer.setDataSource(getContext(), uri, videoInfo.getHeaders());
        }
    }

    /**
     * Set the scale type of the video, needs to be set after setVideo() has been called
     *
     * @param scaleType type to set
     */
    private void setScaleType(@ScaleType int scaleType) {
        if (mMediaPlayer == null) {
            return;
        }
        switch (scaleType) {
            case ScaleType.FIT:
                mMediaPlayer.setVideoScalingMode(MediaPlayer.VIDEO_SCALING_MODE_SCALE_TO_FIT);
                break;
            case ScaleType.CROP:
                mMediaPlayer.setVideoScalingMode(MediaPlayer.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING);
                break;
        }
    }

    private void notifyUnableToOpenContent() {
        if (LogUtil.DEBUG) {
            LogUtil.logw(TAG, "Unable to open content.");
        }
        mCurrentState = PlayerState.ERROR;
        mTargetState = PlayerState.ERROR;
        this.onError(mMediaPlayer, MediaPlayer.MEDIA_ERROR_UNKNOWN, 0);
    }

    public void volumeUp() {
        float nextLevel = mVolumeScalar + VOLUME_SCALE_INTERVAL;
        mVolumeScalar = nextLevel > 1.0f ? 1.0f : nextLevel;
        mMediaPlayer.setVolume(mVolumeScalar, mVolumeScalar);
    }

    public void volumeDown() {
        float nextLevel = mVolumeScalar - VOLUME_SCALE_INTERVAL;
        mVolumeScalar = nextLevel < 0.0f ? 0.0f : nextLevel;
        mMediaPlayer.setVolume(mVolumeScalar, mVolumeScalar);
    }

    public void mute() {
        mMediaPlayer.setVolume(0.0f, 0.0f);
    }

    public void unmute() {
        mMediaPlayer.setVolume(mVolumeScalar, mVolumeScalar);
    }


    // ---------- Player Controller ----------

    public void setPlayerController(final IVideoPlayerController controller) {
        if (LogUtil.DEBUG) {
            LogUtil.log(TAG, "setPlayerController");
        }
        hidePlayerController();
        mPlayerController = controller;
        attachPlayerController();
        hidePlayerController();
    }

    private void attachPlayerController() {
        if (mMediaPlayer != null && mPlayerController != null) {
            mPlayerController.setMediaPlayer(this);
//            View anchorView = this.getParent() instanceof View ? (View) this.getParent() : this;
//            mPlayerController.setAnchorView(anchorView);
            mPlayerController.setEnabled(isInPlaybackState());
        }
    }

    private void showStickyPlayerController() {
        if (mPlayerController != null) {
            mPlayerController.show(0);
        }
    }

    private void showPlayerController() {
        if (mPlayerController != null) {
            mPlayerController.show();
        }
    }

    private void hidePlayerController() {
        if (mPlayerController != null) {
            mPlayerController.hide();
        }
    }

    private void showPlayerControllerLoading() {
        if (mPlayerController != null) {
            mPlayerController.showLoadingView();
        }
    }

    private void hidePlayerControllerLoading() {
        if (mPlayerController != null) {
            mPlayerController.hideLoadingView();
        }
    }

    private void enablePlayerController() {
        if (mPlayerController != null) {
            mPlayerController.setEnabled(true);
        }
    }

    private void disablePlayerController() {
        if (mPlayerController != null) {
            mPlayerController.setEnabled(false);
        }
    }


    // ---------- VideoStatusListener ----------

    public void setVideoStatusListener(IVideoStatusListener listener) {
        this.mVideoStatusListener = listener;
    }


    // ----------  ----------

    private boolean isInPlaybackState() {
        return (mMediaPlayer != null &&
                mCurrentState != PlayerState.ERROR &&
                mCurrentState != PlayerState.IDLE &&
                mCurrentState != PlayerState.PREPARING);
    }

    private boolean pausedAt(final int seekToPosition) {
        return !isPlaying() && (seekToPosition != 0 || getCurrentPosition() > 0);
    }
}
