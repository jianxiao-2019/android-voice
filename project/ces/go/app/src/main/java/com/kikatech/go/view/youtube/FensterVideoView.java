package com.kikatech.go.view.youtube;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.AssetFileDescriptor;
import android.content.res.TypedArray;
import android.graphics.SurfaceTexture;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.support.annotation.IntDef;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
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
import com.kikatech.go.view.youtube.interfaces.VideoPlayer;
import com.kikatech.go.view.youtube.interfaces.VideoStatusListener;

import java.io.IOException;
import java.util.Map;

/**
 * Displays a video file.  The VideoView class
 * can load images from various sources (such as resources or content
 * providers), takes care of computing its measurement from the video so that
 * it can be used in any layout manager, and provides various display options
 * such as scaling and tinting.<p>
 * <p/>
 * <em>Note: VideoView does not retain its full state when going into the
 * background.</em>  In particular, it does not restore the current play state,
 * play position, selected tracks added via
 * {@link android.app.Activity#onSaveInstanceState} and
 * {@link android.app.Activity#onRestoreInstanceState}.<p>
 * Also note that the audio session id (from {@link #getAudioSessionId}) may
 * change from its previously returned value when the VideoView is restored.
 */

/**
 * @author SkeeterWang Created on 2017/6/16.
 */

public class FensterVideoView extends TextureView implements MediaController.MediaPlayerControl, VideoPlayer {
    public static final String TAG = "FensterVideoView";

    public static final int VIDEO_BEGINNING = 0;

    private static final int SCALE_TYPE_FIT = 0;
    private static final int SCALE_TYPE_CROP = 1;

    @IntDef({SCALE_TYPE_FIT, SCALE_TYPE_CROP})
    private @interface ScaleType {
        int DEFAULT = SCALE_TYPE_FIT;
        int FIT = SCALE_TYPE_FIT;
        int CROP = SCALE_TYPE_CROP;
    }

    private static final int PLAYER_SIZE_FULLSCREEN = 0;
    private static final int PLAYER_SIZE_MEDIUM = 1;
    private static final int PLAYER_SIZE_MINIMUM = 2;

    @IntDef({PLAYER_SIZE_FULLSCREEN, PLAYER_SIZE_MEDIUM, PLAYER_SIZE_MINIMUM})
    public @interface PlayerSize {
        int DEFAULT = PLAYER_SIZE_MINIMUM;
        int MINIMUM = PLAYER_SIZE_MINIMUM;
        int MEDIUM = PLAYER_SIZE_MEDIUM;
        int FULLSCREEN = PLAYER_SIZE_FULLSCREEN;
    }

    /* all possible internal states */
    private static final int STATE_ERROR = -1;
    private static final int STATE_IDLE = 0;
    private static final int STATE_PREPARING = 1;
    private static final int STATE_PREPARED = 2;
    private static final int STATE_PLAYING = 3;
    private static final int STATE_PAUSED = 4;
    private static final int STATE_PLAYBACK_COMPLETED = 5;

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

    /*
     * mCurrentState is a VideoView object's current state.
     * mTargetState is the state that a method caller intends to reach.
     *
     * For instance, regardless the VideoView object's current state,
     * calling pause() intends to bring the object to a target state
     * of STATE_PAUSED.
     */
    @PlayerState
    private int mCurrentState = PlayerState.IDLE;
    @PlayerState
    private int mTargetState = PlayerState.IDLE;


    // collaborators / delegates / composites .. discuss
    private final VideoSizeCalculator videoSizeCalculator;

    @ScaleType
    private int mScaleType;

    private Uri mUri;

    private AssetFileDescriptor mAssetFileDescriptor;
    private Map<String, String> mHeaders;
    private SurfaceTexture mSurfaceTexture;
    private MediaPlayer mMediaPlayer = null;
    private FloatingPlayerController fensterPlayerController;
    private MediaPlayer.OnSeekCompleteListener mOnSeekCompleteListener;
    private MediaPlayer.OnTimedTextListener mOnTimeTextListener;
    private MediaPlayer.OnCompletionListener mOnCompletionListener;
    private MediaPlayer.OnPreparedListener mOnPreparedListener;
    private MediaPlayer.OnErrorListener mOnErrorListener;
    private MediaPlayer.OnInfoListener mOnInfoListener;
    private int mAudioSession;
    private int mSeekWhenPrepared;  // recording the seek position while preparing
    private int mCurrentBufferPercentage;
    private boolean mCanPause;
    private boolean mCanSeekBack;
    private boolean mCanSeekForward;
    private VideoStatusListener onPlayStateListener;

    private AlertDialog errorDialog;

    public FensterVideoView(final Context context, final AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FensterVideoView(final Context context, final AttributeSet attrs, final int defStyle) {
        super(context, attrs, defStyle);
        applyCustomAttributes(context, attrs);
        videoSizeCalculator = new VideoSizeCalculator();
        initVideoView();
    }

    private void applyCustomAttributes(Context context, AttributeSet attrs) {
        if (attrs != null) {
            TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.FensterVideoView);
            mScaleType = typedArray.getInt(R.styleable.FensterVideoView_scaleType, ScaleType.DEFAULT);
            typedArray.recycle();
        }
    }

    private void initVideoView() {
        videoSizeCalculator.setVideoSize(0, 0);

        setSurfaceTextureListener(mSTListener);

        setFocusable(true);
        setFocusableInTouchMode(true);
        requestFocus();
        mCurrentState = PlayerState.DEFAULT;
        mTargetState = PlayerState.DEFAULT;
        setOnInfoListener(onInfoToPlayStateListener);
    }

    @Override
    protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec) {
        VideoSizeCalculator.Dimens dimens = videoSizeCalculator.measure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(dimens.getWidth(), dimens.getHeight());
    }

    @Override
    public void onInitializeAccessibilityEvent(final AccessibilityEvent event) {
        super.onInitializeAccessibilityEvent(event);
        event.setClassName(FensterVideoView.class.getName());
    }

    @Override
    public void onInitializeAccessibilityNodeInfo(final AccessibilityNodeInfo info) {
        super.onInitializeAccessibilityNodeInfo(info);
        info.setClassName(FensterVideoView.class.getName());
    }

    public int resolveAdjustedSize(final int desiredSize, final int measureSpec) {
        return getDefaultSize(desiredSize, measureSpec);
    }

    private void disableFileDescriptor() {
        mAssetFileDescriptor = null;
    }

    private String mCaptionFilePath;

    public void setVideo(final String path, final String captionFilePath) {
        mCaptionFilePath = captionFilePath;
        setVideo(path);
    }

    public void setVideo(final String path) {
        disableFileDescriptor();
        setVideo(Uri.parse(path), VIDEO_BEGINNING);
    }

    public void setVideo(final String url, final int seekInSeconds) {
        disableFileDescriptor();
        setVideo(Uri.parse(url), seekInSeconds);
    }

    public void setVideo(final Uri uri, final int seekInSeconds) {
        disableFileDescriptor();
        setVideoURI(uri, null, seekInSeconds);
    }

    public void setVideo(final AssetFileDescriptor assetFileDescriptor) {
        mAssetFileDescriptor = assetFileDescriptor;
        setVideoURI(null, null, VIDEO_BEGINNING);
    }

    public void setVideo(final AssetFileDescriptor assetFileDescriptor, final int seekInSeconds) {
        mAssetFileDescriptor = assetFileDescriptor;
        setVideoURI(null, null, seekInSeconds);
    }

    /**
     * Set the scale type of the video, needs to be set after setVideo() has been called
     *
     * @param scaleType type to set
     */
    private void setScaleType(@ScaleType int scaleType) {
        switch (scaleType) {
            case ScaleType.FIT:
                mMediaPlayer.setVideoScalingMode(MediaPlayer.VIDEO_SCALING_MODE_SCALE_TO_FIT);
                break;
            case ScaleType.CROP:
                mMediaPlayer.setVideoScalingMode(MediaPlayer.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING);
                break;
        }
    }

    private void setVideoURI(final Uri uri, final Map<String, String> headers, final int seekInSeconds) {
        Log.d(TAG, "start playing: " + uri);
        mUri = uri;
        mHeaders = headers;
        mSeekWhenPrepared = seekInSeconds * 1000;
        openVideo();
        requestLayout();
        invalidate();
    }

    public void stopPlayback() {
        if (mMediaPlayer != null) {
            mMediaPlayer.stop();
            mMediaPlayer.release();
            mMediaPlayer = null;
            setKeepScreenOn(false);
            mCurrentState = PlayerState.IDLE;
            mTargetState = PlayerState.IDLE;
        }
    }

    private void openVideo() {
        if (notReadyForPlaybackJustYetWillTryAgainLater()) {
            return;
        }

        // we shouldn't clear the target state, because somebody might have called start() previously
        release(false);
        try {
            mMediaPlayer = new MediaPlayer();

            if (mAudioSession != 0) {
                mMediaPlayer.setAudioSessionId(mAudioSession);
            } else {
                mAudioSession = mMediaPlayer.getAudioSessionId();
            }
            mMediaPlayer.setOnPreparedListener(mPreparedListener);
            mMediaPlayer.setOnVideoSizeChangedListener(mSizeChangedListener);
            mMediaPlayer.setOnCompletionListener(mCompletionListener);
            mMediaPlayer.setOnErrorListener(mErrorListener);
            mMediaPlayer.setOnInfoListener(mInfoListener);
            mMediaPlayer.setOnBufferingUpdateListener(mBufferingUpdateListener);
            mMediaPlayer.setOnSeekCompleteListener(mOnSeekCompleteListener);
            mCurrentBufferPercentage = 0;

            setDataSource();
            setScaleType(mScaleType);

            mMediaPlayer.setSurface(new Surface(mSurfaceTexture));
            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mMediaPlayer.setScreenOnWhilePlaying(true);
            mMediaPlayer.prepareAsync();

            // work around due to MediaPlayer.MEDIA_INFO_BUFFERING_START
            // might not work on some devices
            if (fensterPlayerController != null) {
                fensterPlayerController.showLoadingView();
            }

            if (!TextUtils.isEmpty(mCaptionFilePath)) {
                try {
                    mMediaPlayer.setOnTimedTextListener(mOnTimeTextListener);
                    mMediaPlayer.addTimedTextSource(mCaptionFilePath, MediaPlayer.MEDIA_MIMETYPE_TEXT_SUBRIP);
                    int textTrackIndex = findTrackIndexFor(MediaPlayer.TrackInfo.MEDIA_TRACK_TYPE_TIMEDTEXT, mMediaPlayer.getTrackInfo());
                    if (textTrackIndex >= 0) {
                        mMediaPlayer.selectTrack(textTrackIndex);
                    } else {
                        if (LogUtil.DEBUG) {
                            LogUtil.logw(TAG, "Cannot find text track!");
                        }
                    }
                } catch (Exception e) {
                    if (LogUtil.DEBUG) {
                        LogUtil.printStackTrace(TAG, e.getMessage(), e);
                    }
                }
            }

            // we don't set the target state here either, but preserve the target state that was there before.
            mCurrentState = PlayerState.PREPARING;
            attachMediaController();
        } catch (final IOException | IllegalArgumentException ex) {
            notifyUnableToOpenContent(ex);
        }
    }

    private int findTrackIndexFor(int mediaTrackType, MediaPlayer.TrackInfo[] trackInfo) {
        int index = -1;
        for (int i = 0; i < trackInfo.length; i++) {
            if (trackInfo[i].getTrackType() == mediaTrackType) {
                return i;
            }
        }
        return index;
    }

    private void setDataSource() throws IOException {
        if (mAssetFileDescriptor != null) {
            mMediaPlayer.setDataSource(
                    mAssetFileDescriptor.getFileDescriptor(),
                    mAssetFileDescriptor.getStartOffset(),
                    mAssetFileDescriptor.getLength()
            );
        } else if (mUri != null) {
            mMediaPlayer.setDataSource(getContext(), mUri, mHeaders);
        }
    }

    private boolean notReadyForPlaybackJustYetWillTryAgainLater() {
        return mSurfaceTexture == null;
    }

    private void notifyUnableToOpenContent(final Exception ex) {
        Log.w("Unable to open content:" + mUri, ex);
        mCurrentState = PlayerState.ERROR;
        mTargetState = PlayerState.ERROR;
        mErrorListener.onError(mMediaPlayer, MediaPlayer.MEDIA_ERROR_UNKNOWN, 0);
    }

    public void setMediaController(final FloatingPlayerController controller) {
        if (LogUtil.DEBUG) {
            LogUtil.log(TAG, "setMediaController");
        }
        hideMediaController();
        fensterPlayerController = controller;
        attachMediaController();
        hideMediaController();
    }

    private void attachMediaController() {
        if (mMediaPlayer != null && fensterPlayerController != null) {
            fensterPlayerController.setMediaPlayer(this);
            //            View anchorView = this.getParent() instanceof View ? (View) this.getParent() : this;
            //            fensterPlayerController.setAnchorView(anchorView);
            fensterPlayerController.setEnabled(isInPlaybackState());
        }
    }

    private MediaPlayer.OnPreparedListener mPreparedListener = new MediaPlayer.OnPreparedListener() {
        @Override
        public void onPrepared(final MediaPlayer mediaPlayer) {
            performOnPrepared(mediaPlayer);
        }
    };

    private void performOnPrepared(MediaPlayer mediaPlayer) {
        if (LogUtil.DEBUG) {
            LogUtil.log(TAG, "performOnPrepared, mTargetState: " + mTargetState);
        }
        mCurrentState = PlayerState.PREPARED;

        mCanPause = true;
        mCanSeekBack = true;
        mCanSeekForward = true;

        if (mOnPreparedListener != null) {
            mOnPreparedListener.onPrepared(mMediaPlayer);
        }
        if (fensterPlayerController != null) {
            fensterPlayerController.setEnabled(true);
        }

        videoSizeCalculator.setVideoSize(mediaPlayer.getVideoWidth(), mediaPlayer.getVideoHeight());

        int seekToPosition = mSeekWhenPrepared;  // mSeekWhenPrepared may be changed after seekTo() call

        if (seekToPosition != 0) seekTo(seekToPosition);

        if (mTargetState == PlayerState.PLAYING) {
            start();
        } else if (pausedAt(seekToPosition)) {
            showStickyMediaController();
        }
    }

    private MediaPlayer.OnCompletionListener mCompletionListener = new MediaPlayer.OnCompletionListener() {
        @Override
        public void onCompletion(final MediaPlayer mediaPlayer) {
            performOnCompletion(mediaPlayer);
        }
    };

    private void performOnCompletion(MediaPlayer mediaPlayer) {
        setKeepScreenOn(false);
        mCurrentState = PlayerState.PLAYBACK_COMPLETED;
        mTargetState = PlayerState.PLAYBACK_COMPLETED;
        hideMediaController();
        if (mOnCompletionListener != null) mOnCompletionListener.onCompletion(mMediaPlayer);
    }

    private MediaPlayer.OnVideoSizeChangedListener mSizeChangedListener = new MediaPlayer.OnVideoSizeChangedListener() {
        @Override
        public void onVideoSizeChanged(final MediaPlayer mediaPlayer, final int width, final int height) {
            performOnVideoSizeChanged(mediaPlayer, width, height);
        }
    };

    private void performOnVideoSizeChanged(MediaPlayer mediaPlayer, final int width, final int height) {
        videoSizeCalculator.setVideoSize(mediaPlayer.getVideoWidth(), mediaPlayer.getVideoHeight());
        if (videoSizeCalculator.hasASizeYet()) {
            requestLayout();
        }
    }

    private MediaPlayer.OnErrorListener mErrorListener = new MediaPlayer.OnErrorListener() {
        @Override
        public boolean onError(final MediaPlayer mediaPlayer, final int frameworkError, final int implError) {
            return performOnError(mediaPlayer, frameworkError, implError);
        }
    };

    private boolean performOnError(MediaPlayer mediaPlayer, final int frameworkError, final int implError) {
        if (LogUtil.DEBUG) {
            LogUtil.logw(TAG, "[onError] frameworkError: " + frameworkError + ", implError: " + implError);
        }
        if (mCurrentState == PlayerState.ERROR) {
            return true;
        }
        mCurrentState = PlayerState.ERROR;
        mTargetState = PlayerState.ERROR;
        hideMediaController();

        if (allowPlayStateToHandle(frameworkError)) {
            return true;
        }

        if (allowErrorListenerToHandle(frameworkError, implError)) {
            return true;
        }

        handleError(frameworkError);
        return true;
    }

    private MediaPlayer.OnInfoListener mInfoListener = new MediaPlayer.OnInfoListener() {
        @Override
        public boolean onInfo(final MediaPlayer mp, final int arg1, final int arg2) {
            if (mOnInfoListener != null) {
                mOnInfoListener.onInfo(mp, arg1, arg2);
            }
            return true;
        }
    };

    private final MediaPlayer.OnInfoListener onInfoToPlayStateListener = new MediaPlayer.OnInfoListener() {
        @Override
        public boolean onInfo(final MediaPlayer mp, final int what, final int extra) {
            return performOnInfo(mp, what, extra);
        }
    };

    private boolean performOnInfo(final MediaPlayer mp, final int what, final int extra) {
        if (noPlayStateListener()) {
            return false;
        }

        if (MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START == what) {
            onPlayStateListener.onFirstVideoFrameRendered();
            onPlayStateListener.onPlay();
        } else if (MediaPlayer.MEDIA_INFO_BUFFERING_START == what) {
            onPlayStateListener.onBuffer();
        } else if (MediaPlayer.MEDIA_INFO_BUFFERING_END == what) {
            onPlayStateListener.onPlay();
        }

        return false;
    }

    private boolean pausedAt(final int seekToPosition) {
        return !isPlaying() && (seekToPosition != 0 || getCurrentPosition() > 0);
    }

    private void showStickyMediaController() {
        if (fensterPlayerController != null) {
            fensterPlayerController.show(0);
        }
    }

    private void hideMediaController() {
        if (fensterPlayerController != null) {
            fensterPlayerController.hide();
        }
    }

    private void showMediaController() {
        if (fensterPlayerController != null) {
            fensterPlayerController.show();
        }
    }

    private boolean allowPlayStateToHandle(final int frameworkError) {
        if (frameworkError == MediaPlayer.MEDIA_ERROR_UNKNOWN || frameworkError == MediaPlayer.MEDIA_ERROR_IO) {
            Log.e(TAG, "TextureVideoView error. File or network related operation errors.");
            if (hasPlayStateListener()) {
                return onPlayStateListener.onStopWithExternalError(mMediaPlayer.getCurrentPosition() / TimeUtil.MILLIS_IN_SECOND);
            }
        }
        return false;
    }

    private boolean allowErrorListenerToHandle(final int frameworkError, final int implError) {
        if (mOnErrorListener != null) {
            return mOnErrorListener.onError(mMediaPlayer, frameworkError, implError);
        }

        return false;
    }

    private void handleError(final int frameworkError) {
        if (getWindowToken() != null) {
            if (errorDialog != null && errorDialog.isShowing()) {
                Log.d(TAG, "Dismissing last error dialog for a new one");
                errorDialog.dismiss();
            }
            errorDialog = createErrorDialog(this.getContext(), mOnCompletionListener, mMediaPlayer, getErrorMessage(frameworkError));
            errorDialog.show();
        }
    }

    private static AlertDialog createErrorDialog(final Context context, final MediaPlayer.OnCompletionListener completionListener, final MediaPlayer mediaPlayer, final String errorMessage) {
        return new AlertDialog.Builder(context)
                .setMessage(errorMessage)
                .setPositiveButton(
                        android.R.string.ok,
                        new DialogInterface.OnClickListener() {
                            public void onClick(final DialogInterface dialog, final int whichButton) {
                                    /* If we get here, there is no onError listener, so
                                     * at least inform them that the video is over.
                                     */
                                if (completionListener != null) {
                                    completionListener.onCompletion(mediaPlayer);
                                }
                            }
                        }
                )
                .setCancelable(false)
                .create();
    }

    private static String getErrorMessage(final int frameworkError) {
        String message = "Impossible to play the video.";

        if (frameworkError == MediaPlayer.MEDIA_ERROR_IO) {
            Log.e(TAG, "TextureVideoView error. File or network related operation errors.");
        } else if (frameworkError == MediaPlayer.MEDIA_ERROR_MALFORMED) {
            Log.e(TAG, "TextureVideoView error. Bitstream is not conforming to the related coding standard or file spec.");
        } else if (frameworkError == MediaPlayer.MEDIA_ERROR_SERVER_DIED) {
            Log.e(TAG, "TextureVideoView error. Media server died. In this case, the application must release the MediaPlayer object and instantiate a new one.");
        } else if (frameworkError == MediaPlayer.MEDIA_ERROR_TIMED_OUT) {
            Log.e(TAG, "TextureVideoView error. Some operation takes too long to complete, usually more than 3-5 seconds.");
        } else if (frameworkError == MediaPlayer.MEDIA_ERROR_UNKNOWN) {
            Log.e(TAG, "TextureVideoView error. Unspecified media player error.");
        } else if (frameworkError == MediaPlayer.MEDIA_ERROR_UNSUPPORTED) {
            Log.e(TAG, "TextureVideoView error. Bitstream is conforming to the related coding standard or file spec, but the media framework does not support the feature.");
        } else if (frameworkError == MediaPlayer.MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK) {
            Log.e(TAG, "TextureVideoView error. The video is streamed and its container is not valid for progressive playback i.e the video's index (e.g moov atom) is not at the start of the file.");
            message = "Impossible to play the progressive video.";
        }
        return message;
    }

    private MediaPlayer.OnBufferingUpdateListener mBufferingUpdateListener = new MediaPlayer.OnBufferingUpdateListener() {
        @Override
        public void onBufferingUpdate(final MediaPlayer mp, final int percent) {
            mCurrentBufferPercentage = percent;
        }
    };

    /**
     * Register a callback to be invoked when the media file
     * is loaded and ready to go.
     *
     * @param l The callback that will be run
     */
    public void setOnPreparedListener(final MediaPlayer.OnPreparedListener l) {
        mOnPreparedListener = l;
    }

    public void setOnSeekCompleteListener(final MediaPlayer.OnSeekCompleteListener l) {
        mOnSeekCompleteListener = l;
    }

    /**
     * Register a callback to be invoked when the end of a media file
     * has been reached during playback.
     *
     * @param l The callback that will be run
     */
    public void setOnCompletionListener(final MediaPlayer.OnCompletionListener l) {
        mOnCompletionListener = l;
    }

    public void setOnTimeTextListener(final MediaPlayer.OnTimedTextListener l) {
        mOnTimeTextListener = l;
    }

    /**
     * Register a callback to be invoked when an error occurs
     * during playback or setup.  If no listener is specified,
     * or if the listener returned false, VideoView will inform
     * the user of any errors.
     *
     * @param l The callback that will be run
     */
    public void setOnErrorListener(final MediaPlayer.OnErrorListener l) {
        mOnErrorListener = l;
    }

    /**
     * Register a callback to be invoked when an informational event
     * occurs during playback or setup.
     *
     * @param l The callback that will be run
     */
    private void setOnInfoListener(final MediaPlayer.OnInfoListener l) {
        mOnInfoListener = l;
    }

    private SurfaceTextureListener mSTListener = new SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(final SurfaceTexture surface, final int width, final int height) {
            mSurfaceTexture = surface;
//            openVideo();
        }

        @Override
        public void onSurfaceTextureSizeChanged(final SurfaceTexture surface, final int width, final int height) {
            boolean isValidState = (mTargetState == PlayerState.PLAYING);
            boolean hasValidSize = videoSizeCalculator.currentSizeIs(width, height);
            if (mMediaPlayer != null && isValidState && hasValidSize) {
                if (mSeekWhenPrepared != 0) {
                    seekTo(mSeekWhenPrepared);
                }
                start();
            }
        }

        @Override
        public boolean onSurfaceTextureDestroyed(final SurfaceTexture surface) {
            mSurfaceTexture = null;
            hideMediaController();
            release(true);
            return false;
        }

        @Override
        public void onSurfaceTextureUpdated(final SurfaceTexture surface) {
            mSurfaceTexture = surface;
        }
    };

    /*
     * release the media player in any state
     */
    private void release(final boolean clearTargetState) {
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
    public boolean onTrackballEvent(final MotionEvent ev) {
        if (LogUtil.DEBUG) {
            LogUtil.log(TAG, "onTrackballEvent, ev: " + ev.toString());
        }
        if (isInPlaybackState() && fensterPlayerController != null) {
            fensterPlayerController.show();
        }
        return false;
    }

    @Override
    public boolean onKeyDown(final int keyCode, final KeyEvent event) {
        boolean isKeyCodeSupported = keyCode != KeyEvent.KEYCODE_BACK &&
                keyCode != KeyEvent.KEYCODE_VOLUME_UP &&
                keyCode != KeyEvent.KEYCODE_VOLUME_DOWN &&
                keyCode != KeyEvent.KEYCODE_VOLUME_MUTE &&
                keyCode != KeyEvent.KEYCODE_MENU &&
                keyCode != KeyEvent.KEYCODE_CALL &&
                keyCode != KeyEvent.KEYCODE_ENDCALL;
        if (isInPlaybackState() && isKeyCodeSupported && fensterPlayerController != null) {
            if (keyCode == KeyEvent.KEYCODE_HEADSETHOOK || keyCode == KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE) {
                if (mMediaPlayer.isPlaying()) {
                    pause();
                    showMediaController();
                } else {
                    start();
                    hideMediaController();
                }
                return true;
            } else if (keyCode == KeyEvent.KEYCODE_MEDIA_PLAY) {
                if (!mMediaPlayer.isPlaying()) {
                    start();
                    hideMediaController();
                }
                return true;
            } else if (keyCode == KeyEvent.KEYCODE_MEDIA_STOP || keyCode == KeyEvent.KEYCODE_MEDIA_PAUSE) {
                if (mMediaPlayer.isPlaying()) {
                    pause();
                    showMediaController();
                }
                return true;
            } else {
                fensterPlayerController.show();
            }
        }

        return super.onKeyDown(keyCode, event);
    }

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
                onPlayStateListener.onPause();
            }
        }
        mTargetState = PlayerState.PAUSED;
    }

    public void suspend() {
        release(false);
    }

    public void resume() {
        openVideo();
    }

    @Override
    public int getDuration() {
        if (isInPlaybackState()) {
            return mMediaPlayer.getDuration();
        }
        return -1;
    }

    /**
     * @return current position in milliseconds
     */
    @Override
    public int getCurrentPosition() {
        if (mMediaPlayer != null && mCurrentState == PlayerState.PLAYBACK_COMPLETED) {
            return mMediaPlayer.getDuration();
        } else if (isInPlaybackState()) {
            return mMediaPlayer.getCurrentPosition();
        }
        return 0;
    }

    public int getCurrentPositionInSeconds() {
        return getCurrentPosition() / TimeUtil.MILLIS_IN_SECOND;
    }

    @Override
    public void seekTo(final int millis) {
        if (isInPlaybackState()) {
            mMediaPlayer.seekTo(millis);
            mSeekWhenPrepared = 0;
        } else {
            mSeekWhenPrepared = millis;
        }
    }

    public void seekToSeconds(final int seconds) {
        seekTo(seconds * TimeUtil.MILLIS_IN_SECOND);
        mMediaPlayer.setOnSeekCompleteListener(new MediaPlayer.OnSeekCompleteListener() {
            @Override
            public void onSeekComplete(final MediaPlayer mp) {
                Log.i(TAG, "seek completed");
            }
        });
    }

    @Override
    public boolean isPlaying() {
        return isInPlaybackState() && mMediaPlayer.isPlaying();
    }

    public boolean isPrepared() {
        return isInPlaybackState() && mCurrentState == PlayerState.PREPARED;
    }


    @Override
    public int getBufferPercentage() {
        if (mMediaPlayer != null) {
            return mCurrentBufferPercentage;
        }
        return 0;
    }

    private boolean isInPlaybackState() {
        return (mMediaPlayer != null &&
                mCurrentState != PlayerState.ERROR &&
                mCurrentState != PlayerState.IDLE &&
                mCurrentState != PlayerState.PREPARING);
    }

    @Override
    public boolean canPause() {
        return mCanPause;
    }

    @Override
    public boolean canSeekBackward() {
        return mCanSeekBack;
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

    private boolean noPlayStateListener() {
        return !hasPlayStateListener();
    }

    private boolean hasPlayStateListener() {
        return onPlayStateListener != null;
    }

    public void setOnPlayStateListener(final VideoStatusListener onPlayStateListener) {
        this.onPlayStateListener = onPlayStateListener;
    }
}
