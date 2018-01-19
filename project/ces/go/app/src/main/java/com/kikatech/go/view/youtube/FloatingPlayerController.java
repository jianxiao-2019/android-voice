package com.kikatech.go.view.youtube;

import android.content.Context;
import android.graphics.RectF;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.kikatech.go.R;
import com.kikatech.go.util.AnimationUtils;
import com.kikatech.go.util.LogUtil;
import com.kikatech.go.view.youtube.interfaces.VideoPlayer;
import com.kikatech.go.view.youtube.interfaces.VideoPlayerController;
import com.kikatech.go.view.youtube.interfaces.VideoPlayerControllerVisibilityListener;
import com.kikatech.go.view.youtube.interfaces.VideoStatusListener;

import java.util.Formatter;
import java.util.Locale;

/**
 * @author wangskeeter Created on 16/5/16.
 */

public class FloatingPlayerController extends FrameLayout implements VideoPlayerController, VideoStatusListener, VideoTouchRoot.OnTouchReceiver {
    public static final String TAG = "PlayerController";

    public static final int DEFAULT_VIDEO_START = 0;
    private static final int DEFAULT_TIMEOUT = 5000;

    private static final int FADE_OUT = 1;
    private static final int SHOW_PROGRESS = 2;

    private VideoPlayerControllerVisibilityListener visibilityListener;
    private IControllerCallback mControllerListener;
    private IPlayerStatusCallback mPlayerStatusCallback;
    private VideoPlayer mFensterPlayer;
    private boolean mShowing;
    private boolean mDragging;

    private boolean mLoading;
    private boolean mFirstTimeLoading = true;


    @FensterVideoView.PlayerSize
    private int mPlayerSize;

    // ----- Views -----

    private static ScaleAnimation scaleAnimation;
    private StringBuilder mFormatBuilder;
    private Formatter mFormatter;
    private int lastPlayedSeconds = -1;

    // Top Area
    //----------------------------------------
    private View mBtnRepeat;
    private View mBtnShare;
    private View mBtnShareIconNew;
    private View mBtnFavorite;
    private View mBtnScaleUp;
    private View mBtnScaleDown;
    private View mBtnClose;
    // Center Area
    //----------------------------------------
    private View mBtnPrev;
    private View mBtnNext;
    private View mProgressLoading;
    private ImageView mBtnPauseResume;
    private View mBtnYouTubeIcon;
    // Bottom Area
    //----------------------------------------
    private TextView mTvTimeCurrent;
    private SeekBar mSeekBar;
    private TextView mTvTimeTotal;

    private VideoTouchRoot mTouchRoot;


    // There are two scenarios that can trigger the seekbar listener to trigger:
    //
    // The first is the user using the touchpad to adjust the posititon of the
    // seekbar's thumb. In this case onStartTrackingTouch is called followed by
    // a number of onProgressChanged notifications, concluded by onStopTrackingTouch.
    // We're setting the field "mDragging" to true for the duration of the dragging
    // session to avoid jumps in the position in case of ongoing playback.
    //
    // The second scenario involves the user operating the scroll ball, in this
    // case there WON'T BE onStartTrackingTouch/onStopTrackingTouch notifications,
    // we will simply apply the updated position without suspending regular updates.
    private final SeekBar.OnSeekBarChangeListener mSeekListener = new SeekBar.OnSeekBarChangeListener() {
        public void onStartTrackingTouch(final SeekBar bar) {
            show(3600000);

            mDragging = true;

            // By removing these pending progress messages we make sure
            // that a) we won't update the progress while the user adjusts
            // the seekbar and b) once the user is done dragging the thumb
            // we will post one of these messages to the queue again and
            // this ensures that there will be exactly one message queued up.
            mHandler.removeMessages(SHOW_PROGRESS);
        }

        public void onProgressChanged(final SeekBar bar, final int progress, final boolean fromUser) {
            // We're not interested in programmatically generated changes to
            // the progress bar's position.
            if (!fromUser) {
                return;
            }
            long duration = mFensterPlayer.getDuration();
            long newPosition = (duration * progress) / 1000L;
            mFensterPlayer.seekTo((int) newPosition);
            if (mTvTimeCurrent != null) {
                mTvTimeCurrent.setText(stringForTime((int) newPosition));
            }
        }

        public void onStopTrackingTouch(final SeekBar bar) {
            mDragging = false;
            setProgress();
            updatePausePlay();
            show(DEFAULT_TIMEOUT);

            // Ensure that progress is properly updated in the future,
            // the call to show() does not guarantee this because it is a
            // no-op if we are already showing.
            mHandler.sendEmptyMessage(SHOW_PROGRESS);
        }
    };

    private final Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(final Message msg) {
            processMessage(msg);
        }

        void processMessage(final Message msg) {
            int position;
            switch (msg.what) {
                case FADE_OUT:
                    if (!mFensterPlayer.isPlaying()) // re-schedule to check again
                    {
                        Message fadeMessage = obtainMessage(FADE_OUT);
                        removeMessages(FADE_OUT);
                        sendMessageDelayed(fadeMessage, DEFAULT_TIMEOUT);
                    } else {
                        hide();
                    }
                    break;
                case SHOW_PROGRESS:
                    position = setProgress();
                    if (!mDragging && mShowing && mFensterPlayer.isPlaying()) {
                        final Message message = obtainMessage(SHOW_PROGRESS);
                        sendMessageDelayed(message, 1000 - (position % 1000));
                    }
                    break;
            }
        }
    };


    public FloatingPlayerController(final Context context) {
        this(context, null);
    }

    public FloatingPlayerController(final Context context, final AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FloatingPlayerController(final Context context, final AttributeSet attrs, final int defStyle) {
        super(context, attrs, defStyle);

        if (isInEditMode()) {
            return;
        }

        mPlayerSize = FensterVideoView.PlayerSize.DEFAULT;
        if (scaleAnimation == null) {
            scaleAnimation = new ScaleAnimation(
                    1f, 0.8f, // Start and end values for the X axis scaling
                    1f, 0.8f, // Start and end values for the Y axis scaling
                    Animation.RELATIVE_TO_SELF, 0.5f, // Pivot point of X scaling
                    Animation.RELATIVE_TO_SELF, 0.5f); // Pivot point of Y scaling
            // scaleAnimation.setFillAfter(true); // Needed to keep the result of the animation
            scaleAnimation.setDuration(100);
        }
    }


    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        if (isInEditMode()) {
            return;
        }

        bindView();
        bindListeners();
        initControllerUI();
    }

    @Override
    public boolean onTrackballEvent(final MotionEvent ev) {
        show(DEFAULT_TIMEOUT);
        return false;
    }

    @Override
    public void onInitializeAccessibilityEvent(final AccessibilityEvent event) {
        super.onInitializeAccessibilityEvent(event);
        event.setClassName(FloatingPlayerController.class.getName());
    }

    @Override
    public void onInitializeAccessibilityNodeInfo(final AccessibilityNodeInfo info) {
        super.onInitializeAccessibilityNodeInfo(info);
        info.setClassName(FloatingPlayerController.class.getName());
    }

    @Override
    public boolean dispatchKeyEvent(final KeyEvent event) {
        int keyCode = event.getKeyCode();
        final boolean uniqueDown = event.getRepeatCount() == 0 && event.getAction() == KeyEvent.ACTION_DOWN;
        if (keyCode == KeyEvent.KEYCODE_HEADSETHOOK || keyCode == KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE || keyCode == KeyEvent.KEYCODE_SPACE) {
            if (uniqueDown && mFensterPlayer != null) {
                doPauseResume();
                show(DEFAULT_TIMEOUT);
            }
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_MEDIA_PLAY) {
            if (uniqueDown && !mFensterPlayer.isPlaying()) {
                mFensterPlayer.start();
                updatePausePlay();
                show(DEFAULT_TIMEOUT);
            }
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_MEDIA_STOP || keyCode == KeyEvent.KEYCODE_MEDIA_PAUSE) {
            if (uniqueDown && mFensterPlayer.isPlaying()) {
                mFensterPlayer.pause();
                updatePausePlay();
                show(DEFAULT_TIMEOUT);
            }
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN || keyCode == KeyEvent.KEYCODE_VOLUME_UP || keyCode == KeyEvent.KEYCODE_VOLUME_MUTE || keyCode == KeyEvent.KEYCODE_CAMERA) {
            // don't show the controls for volume adjustment
            return super.dispatchKeyEvent(event);
        } else if (keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_MENU) {
            if (uniqueDown) {
                hide();
            }
            return true;
        }
        show(DEFAULT_TIMEOUT);
        return super.dispatchKeyEvent(event);
    }

    public void setControllerListener(final IControllerCallback callback) {
        this.mControllerListener = callback;
    }

    public void setPlayerStatusCallback(final IPlayerStatusCallback callback) {
        this.mPlayerStatusCallback = callback;
    }

    private void bindView() {
        switch (mPlayerSize) {
            case FensterVideoView.PlayerSize.MINIMUM:
                LayoutInflater.from(getContext()).inflate(R.layout.floating_player_controller_small, this);
                mBtnClose = findViewById(R.id.controller_center_btn_close);
                mBtnPauseResume = (ImageView) findViewById(R.id.controller_center_btn_pause_resume);
                mProgressLoading = findViewById(R.id.controller_center_progress_loading);
                // Top Area
                //----------------------------------------
                mBtnRepeat = null;
                mBtnShare = null;
                mBtnShareIconNew = null;
                mBtnFavorite = null;
                mBtnScaleUp = null;
                mBtnScaleDown = null;
                mBtnClose = findViewById(R.id.controller_center_btn_close);
                // Center Area
                //----------------------------------------
                mBtnPrev = null;
                mBtnNext = null;
                mProgressLoading = findViewById(R.id.controller_center_progress_loading);
                mBtnPauseResume = (ImageView) findViewById(R.id.controller_center_btn_pause_resume);
                mBtnYouTubeIcon = null;
                // Bottom Area
                //----------------------------------------
                mTvTimeCurrent = null;
                mSeekBar = null;
                mTvTimeTotal = null;
                break;
            case FensterVideoView.PlayerSize.MEDIUM:
                LayoutInflater.from(getContext()).inflate(R.layout.floating_player_controller_medium, this);
                // Top Area
                //----------------------------------------
                mBtnRepeat = findViewById(R.id.controller_top_btn_repeat);
                mBtnShare = findViewById(R.id.controller_top_btn_share);
                mBtnShareIconNew = findViewById(R.id.controller_top_btn_share_icon_new);
                mBtnFavorite = findViewById(R.id.controller_top_btn_favorite);
                mBtnScaleUp = findViewById(R.id.controller_top_btn_scale_up);
                mBtnScaleDown = findViewById(R.id.controller_top_btn_scale_down);
                mBtnClose = findViewById(R.id.controller_top_btn_close);
                // Center Area
                //----------------------------------------
                mBtnPrev = findViewById(R.id.controller_center_btn_previous);
                mBtnNext = findViewById(R.id.controller_center_btn_next);
                mProgressLoading = findViewById(R.id.controller_center_progress_loading);
                mBtnPauseResume = (ImageView) findViewById(R.id.controller_center_btn_pause_resume);
                mBtnYouTubeIcon = findViewById(R.id.controller_center_btn_youtube_icon);
                // Bottom Area
                //----------------------------------------
                mTvTimeCurrent = (TextView) findViewById(R.id.controller_bottom_tv_time_current);
                mSeekBar = (SeekBar) findViewById(R.id.controller_bottom_seek_bar);
                mTvTimeTotal = (TextView) findViewById(R.id.controller_bottom_tv_time_total);
                break;
            case FensterVideoView.PlayerSize.FULLSCREEN:
                LayoutInflater.from(getContext()).inflate(R.layout.floating_player_controller_fullscreen, this);
                // Top Area
                //----------------------------------------
                mBtnRepeat = findViewById(R.id.controller_top_btn_repeat);
                mBtnShare = findViewById(R.id.controller_top_btn_share);
                mBtnShareIconNew = findViewById(R.id.controller_top_btn_share_icon_new);
                mBtnFavorite = findViewById(R.id.controller_top_btn_favorite);
                mBtnScaleUp = findViewById(R.id.controller_top_btn_scale_up);
                mBtnScaleDown = findViewById(R.id.controller_top_btn_scale_down);
                mBtnClose = findViewById(R.id.controller_top_btn_close);
                // Center Area
                //----------------------------------------
                mBtnPrev = findViewById(R.id.controller_center_btn_previous);
                mBtnNext = findViewById(R.id.controller_center_btn_next);
                mProgressLoading = findViewById(R.id.controller_center_progress_loading);
                mBtnPauseResume = (ImageView) findViewById(R.id.controller_center_btn_pause_resume);
                mBtnYouTubeIcon = findViewById(R.id.controller_center_btn_youtube_icon);
                // Bottom Area
                //----------------------------------------
                mTvTimeCurrent = (TextView) findViewById(R.id.controller_bottom_tv_time_current);
                mSeekBar = (SeekBar) findViewById(R.id.controller_bottom_seek_bar);
                mTvTimeTotal = (TextView) findViewById(R.id.controller_bottom_tv_time_total);
                break;
        }
    }

    private void bindListeners() {
        switch (mPlayerSize) {
            case FensterVideoView.PlayerSize.MINIMUM:
                break;
            case FensterVideoView.PlayerSize.MEDIUM:
            case FensterVideoView.PlayerSize.FULLSCREEN:
                mSeekBar.setOnSeekBarChangeListener(mSeekListener);
                break;
        }
        mTouchRoot = (VideoTouchRoot) findViewById(R.id.controller_touch_root);
        mTouchRoot.setOnTouchReceiver(this);
    }

    private void initControllerUI() {
//        boolean isRepeatOn = YouTubeUtils.getYoutubeRepeatMode().equals(YouTubeUtils.RepeatMode.Single);
//        mTopBtnRepeat.setTextColor(getResources().getColor(isRepeatOn ? R.color.gela_green_highlight : android.R.color.white));
        mFormatBuilder = new StringBuilder();
        mFormatter = new Formatter(mFormatBuilder, Locale.getDefault());
        switch (mPlayerSize) {
            case FensterVideoView.PlayerSize.MINIMUM:
                break;
            case FensterVideoView.PlayerSize.MEDIUM:
            case FensterVideoView.PlayerSize.FULLSCREEN:
                mSeekBar.setMax(1000);
                break;
        }
    }

    public void performControllerView(float rawX, float rawY) {
        if (mShowing) {
            View[] possibleButtons = new View[]{
                    // Top Area
                    mBtnRepeat, mBtnShare, mBtnFavorite,
                    mBtnScaleDown, mBtnScaleUp, mBtnClose,
                    // Center Area
                    mBtnPrev, mBtnPauseResume, mBtnNext, mBtnYouTubeIcon};

            View btn = getPossibleBtn(possibleButtons, rawX, rawY);
            if (btn != null) {
                if (btn == mBtnRepeat) {
                    performOnRepeat(btn);
                } else if (btn == mBtnShare) {
                    performOnShare(btn);
                } else if (btn == mBtnFavorite) {
                    performOnFavorite(btn);
                } else if (btn == mBtnScaleDown) {
                    performOnScaleDown(btn);
                } else if (btn == mBtnScaleUp) {
                    performOnScaleUp(btn);
                } else if (btn == mBtnClose) {
                    performOnClose(btn);
                } else if (btn == mBtnPrev) {
                    performOnPrev(btn);
                } else if (btn == mBtnPauseResume) {
                    performOnPause(btn);
                } else if (btn == mBtnNext) {
                    performOnNext(btn);
                } else if (btn == mBtnYouTubeIcon) {
                    performOnYouTubeIconClick();
                }
            } else {
                hide();
            }
        } else {
            show();
        }
    }

    private View getPossibleBtn(View[] possibleBtns, float rawX, float rawY) {
        RectF btnRect;
        try {
            for (View btn : possibleBtns) {
                if (btn == null || btn.getVisibility() != VISIBLE) {
                    continue;
                }
                int[] location = new int[2];
                btn.getLocationOnScreen(location);
                btnRect = new RectF(location[0], location[1], location[0] + btn.getWidth(), location[1] + btn.getHeight());
                if (btnRect.contains(rawX, rawY)) {
                    return btn;
                }
            }
        } catch (Exception e) {
            if (LogUtil.DEBUG) {
                LogUtil.printStackTrace(TAG, e.getMessage(), e);
            }
        }
        return null;
    }

    private void performOnRepeat(View btn) {
        AnimationUtils.getIns().animate(btn, scaleAnimation, new AnimationUtils.IAnimationEndCallback() {
            @Override
            public void onEnd(View view) {
                // TODO
//                boolean isRepeatOn = YouTubeUtils.getYoutubeRepeatMode().equals(YouTubeUtils.RepeatMode.Single);
//
//                isRepeatOn = !isRepeatOn;
//
//                ((FontIcon) view).setTextColor(getResources().getColor(isRepeatOn ? R.color.gela_green_highlight : android.R.color.white));
//                YouTubeUtils.setYoutubeRepeatMode(isRepeatOn ? YouTubeUtils.RepeatMode.Single : YouTubeUtils.RepeatMode.List);
//
//                if (mControllerListener != null) {
//                    mControllerListener.onRepeatModeChanged(isRepeatOn);
//                }
            }
        });
    }

    private void performOnShare(View btn) {
        AnimationUtils.getIns().animate(btn, scaleAnimation, new AnimationUtils.IAnimationEndCallback() {
            @Override
            public void onEnd(View view) {
                // TODO
//                if (!YouTubeUtils.getHasClickYoutubeShareIconNew()) {
//                    YouTubeUtils.setHasClickYoutubeShareIconNew(true);
//                    view.setVisibility(GONE);
//                }
                if (mControllerListener != null) {
                    mControllerListener.onShare();
                }
            }
        });
    }

    private void performOnFavorite(View btn) {
        AnimationUtils.getIns().animate(btn, scaleAnimation, new AnimationUtils.IAnimationEndCallback() {
            @Override
            public void onEnd(View view) {
                if (mControllerListener != null) {
                    mControllerListener.onFavorite();
                }
            }
        });
    }

    private void performOnPrev(View btn) {
        AnimationUtils.getIns().animate(btn, scaleAnimation, new AnimationUtils.IAnimationEndCallback() {
            @Override
            public void onEnd(View view) {
                if (mControllerListener != null) {
                    mControllerListener.onPrev();
                }
            }
        });
    }

    private void performOnPause(View btn) {
        AnimationUtils.getIns().animate(btn, scaleAnimation, new AnimationUtils.IAnimationEndCallback() {
            @Override
            public void onEnd(View view) {
                if (mFensterPlayer == null) {
                    return;
                }
                doPauseResume();
                show(DEFAULT_TIMEOUT);
            }
        });
    }

    private void performOnNext(View btn) {
        AnimationUtils.getIns().animate(btn, scaleAnimation, new AnimationUtils.IAnimationEndCallback() {
            @Override
            public void onEnd(View view) {
                if (mControllerListener != null) {
                    mControllerListener.onNext();
                }
            }
        });
    }

    private void performOnScaleUp(View btn) {
        AnimationUtils.getIns().animate(btn, scaleAnimation, new AnimationUtils.IAnimationEndCallback() {
            @Override
            public void onEnd(View view) {
                if (mControllerListener != null) {
                    mControllerListener.onScaleUp();
                }
            }
        });
    }

    private void performOnScaleDown(View btn) {
        AnimationUtils.getIns().animate(btn, scaleAnimation, new AnimationUtils.IAnimationEndCallback() {
            @Override
            public void onEnd(View view) {
                if (mControllerListener != null) {
                    mControllerListener.onScaleDown();
                }
            }
        });
    }

    private void performOnClose(View btn) {
        AnimationUtils.getIns().animate(btn, scaleAnimation, new AnimationUtils.IAnimationEndCallback() {
            @Override
            public void onEnd(View view) {
                if (mControllerListener != null) {
                    mControllerListener.onClose();
                }
            }
        });
    }

    private void performOnYouTubeIconClick() {
        if (mControllerListener != null) {
            mControllerListener.onYouTubeIconClick();
        }
    }


    private void doPauseResume() {
        if (mFensterPlayer == null) return;

        if (mFensterPlayer.isPlaying()) {
            mFensterPlayer.pause();
        } else {
            mFensterPlayer.start();
        }
        updatePausePlay();
    }

    public void updatePausePlay() {
        if (mFensterPlayer == null) {
            return;
        }

        boolean isPlaying = mFensterPlayer.isPlaying();
        int btnIconRes = isPlaying ? R.drawable.kika_kikago_float_ic_pause : R.drawable.kika_kikago_float_ic_play;

        if (mBtnPauseResume != null) {
            mBtnPauseResume.setImageResource(btnIconRes);
        }
    }

    private String stringForTime(final int timeMs) {
        int totalSeconds = timeMs / 1000;

        int seconds = totalSeconds % 60;
        int minutes = (totalSeconds / 60) % 60;
        int hours = totalSeconds / 3600;

        mFormatBuilder.setLength(0);

        return (hours > 0) ? mFormatter.format("%d:%02d:%02d", hours, minutes, seconds).toString()
                : mFormatter.format("%02d:%02d", minutes, seconds).toString();
    }

    private int setProgress() {
        if (mFensterPlayer == null || mDragging) {
            return 0;
        }

        int position = mFensterPlayer.getCurrentPosition();
        int duration = mFensterPlayer.getDuration();

        if (mSeekBar != null) {
            if (duration > 0) {
                long pos = 1000L * position / duration;    // use long to avoid overflow
                mSeekBar.setProgress((int) pos);
            }
            int percent = mFensterPlayer.getBufferPercentage();
            mSeekBar.setSecondaryProgress(percent * 10);
        }

        if (mTvTimeTotal != null) {
            mTvTimeTotal.setText(stringForTime(duration));
        }

        if (mTvTimeCurrent != null) {
            mTvTimeCurrent.setText(stringForTime(position));
        }

        final int playedSeconds = position / 1000;

        if (lastPlayedSeconds != playedSeconds) {
            lastPlayedSeconds = playedSeconds;
        }

        return position;
    }


    // ----- FensterPlayerController ----

    @Override
    public void setMediaPlayer(final VideoPlayer fensterPlayer) {
        mFensterPlayer = fensterPlayer;
        updatePausePlay();
    }

    @Override
    public void setEnabled(final boolean enabled) {
        /*
        if( mBtnPause != null ) mBtnPause.setEnabled( enabled );

		if( mBtnNext != null ) mBtnNext.setEnabled( enabled );

		if( mBtnPrev != null ) mBtnPrev.setEnabled( enabled );

		if( mProgress != null ) mProgress.setEnabled( enabled );
		*/
        super.setEnabled(enabled);
    }

    /**
     * Show the controller on screen. It will go away
     * automatically after the default time of inactivity.
     */
    @Override
    public void show() {
        show(DEFAULT_TIMEOUT);
    }

    /**
     * Show the controller on screen. It will go away
     * automatically after 'timeout' milliseconds of inactivity.
     *
     * @param timeInMilliSeconds The timeout in milliseconds. Use 0 to show
     *                           the controller until hide() is called.
     */
    @Override
    public void show(final int timeInMilliSeconds) {
        if (LogUtil.DEBUG) {
            LogUtil.logdParent(TAG, "YouTubeMusicProvider, show: " + timeInMilliSeconds + " ms", 10);
        }
        if (!mShowing) {
            setProgress();
            if (mBtnPauseResume != null) {
                mBtnPauseResume.requestFocus();
            }
            mShowing = true;
            setVisibility(VISIBLE);
        }

        updatePausePlay();

        // cause the progress bar to be updated even if mShowing
        // was already true.  This happens, for example, if we're
        // paused with the progress bar showing the user hits play.
        mHandler.sendEmptyMessage(SHOW_PROGRESS);

        Message msg = mHandler.obtainMessage(FADE_OUT);
        if (timeInMilliSeconds != 0) {
            mHandler.removeMessages(FADE_OUT);
            mHandler.sendMessageDelayed(msg, timeInMilliSeconds);
        }

        if (visibilityListener != null) {
            visibilityListener.onControlsVisibilityChange(true);
        }
    }

    /**
     * Remove the controller from the screen.
     */
    @Override
    public void hide() {
        if (mShowing || getVisibility() == VISIBLE) {
            try {
                mHandler.removeMessages(SHOW_PROGRESS);
                setVisibility(View.INVISIBLE);
            } catch (final IllegalArgumentException e) {
                // MediaController, already removed
                if (LogUtil.DEBUG) {
                    LogUtil.printStackTrace(TAG, e.getMessage(), e);
                }
            }
            mShowing = false;
        }

        if (visibilityListener != null) {
            visibilityListener.onControlsVisibilityChange(false);
        }
    }

    @Override
    public void setVisibilityListener(final VideoPlayerControllerVisibilityListener visibilityListener) {
        this.visibilityListener = visibilityListener;
    }


    // ----- FensterVideoStateListener -----

    @Override
    public void onFirstVideoFrameRendered() {
        // controlsRoot: mCenterArea
        mFirstTimeLoading = false;
    }

    @Override
    public void onPlay() {
        hideLoadingView();
        if (mPlayerStatusCallback != null) {
            mPlayerStatusCallback.onPlay();
        }
    }

    @Override
    public void onPause() {
        if (mPlayerStatusCallback != null) {
            mPlayerStatusCallback.onPause();
        }
    }

    @Override
    public void onBuffer() {
        showLoadingView();
        if (mPlayerStatusCallback != null) {
            mPlayerStatusCallback.onBuffer();
        }
    }

    @Override
    public boolean onStopWithExternalError(int position) {
        return false;
    }

    public void hideLoadingView() {
        hide();
        if (mProgressLoading != null) {
            mProgressLoading.setVisibility(GONE);
        }
        mLoading = false;
    }

    public void showLoadingView() {
        mLoading = true;
        if (mProgressLoading != null) {
            mProgressLoading.setVisibility(VISIBLE);
        }
    }


    // TODO: FensterTouchRoot.OnTouchReceiver

    /**
     * Called by ViewTouchRoot on user touches,
     * so we can avoid hiding the ui while the user is interacting.
     */
    @Override
    public void onControllerUiTouched() {
        if (LogUtil.DEBUG) {
            LogUtil.logd(TAG, "[onControllerUiTouched] mShowing ? " + mShowing);
        }

        show();
    }


    public void setPlayerSize(@FensterVideoView.PlayerSize int playerSize) {
        this.mPlayerSize = playerSize;
        bindView();
        bindListeners();
        initControllerUI();
    }

    public boolean isShowing() {
        return mShowing;
    }

    public boolean isLoading() {
        return mLoading;
    }

    public boolean isFirstTimeLoading() {
        return mFirstTimeLoading;
    }


    public interface IControllerCallback {
        void onPrev();

        void onNext();

        void onScaleUp();

        void onScaleDown();

        void onClose();

        void onRepeatModeChanged(boolean isChecked);

        void onShare();

        void onFavorite();

        void onYouTubeIconClick();

        void onLockModeLocked();

        void onLockModeUnlocked();
    }

    public interface IPlayerStatusCallback {
        void onPlay();

        void onPause();

        void onBuffer();
    }
}