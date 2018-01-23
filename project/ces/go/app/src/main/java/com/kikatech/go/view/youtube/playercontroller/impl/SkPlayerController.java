package com.kikatech.go.view.youtube.playercontroller.impl;

import android.content.Context;
import android.graphics.RectF;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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
import com.kikatech.go.view.youtube.VideoTouchRoot;
import com.kikatech.go.view.youtube.player.impl.SkVideoPlayerView;
import com.kikatech.go.view.youtube.player.interfaces.IVideoPlayer;
import com.kikatech.go.view.youtube.player.interfaces.IVideoStatusListener;
import com.kikatech.go.view.youtube.playercontroller.interfaces.IVideoPlayerController;

import java.util.Formatter;
import java.util.Locale;

/**
 * @author SkeeterWang Created on 2018/1/19.
 */

public class SkPlayerController extends FrameLayout implements IVideoPlayerController, IVideoStatusListener, VideoTouchRoot.OnTouchReceiver {
    private static final String TAG = "SkPlayerController";

    private static final int DEFAULT_DISPLAY_TIMEOUT = 5000;
    // --------------------------------------------------
    private static final int MSG_FADE_OUT = 1;
    private static final int MSG_SHOW_PROGRESS = 2;

    // --------------------------------------------------

    @IntDef({MSG_FADE_OUT, MSG_SHOW_PROGRESS})
    private @interface HandlerMsg {
        int FADE_OUT = MSG_FADE_OUT;
        int SHOW_PROGRESS = MSG_SHOW_PROGRESS;
    }

    // --------------------------------------------------

    private final Handler mUiHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(final Message msg) {
            processMessage(msg);
        }

        private void processMessage(final Message msg) {
            int position;
            switch (msg.what) {
                case HandlerMsg.FADE_OUT:
                    // re-schedule to check again
                    if (mVideoPlayer != null && !mVideoPlayer.isPlaying()) {
                        Message fadeMessage = obtainMessage(HandlerMsg.FADE_OUT);
                        removeMessages(HandlerMsg.FADE_OUT);
                        sendMessageDelayed(fadeMessage, DEFAULT_DISPLAY_TIMEOUT);
                    } else {
                        hide();
                    }
                    break;
                case HandlerMsg.SHOW_PROGRESS:
                    position = setProgress();
                    if (!mDragging && mShowing && mVideoPlayer != null && mVideoPlayer.isPlaying()) {
                        final Message message = obtainMessage(HandlerMsg.SHOW_PROGRESS);
                        sendMessageDelayed(message, 1000 - (position % 1000));
                    }
                    break;
            }
        }
    };
    // --------------------------------------------------


    @SkVideoPlayerView.PlayerSize
    private int mPlayerSize = SkVideoPlayerView.PlayerSize.DEFAULT;


    private static ScaleAnimation mScaleAnimation; // button click animation
    private StringBuilder mFormatBuilder; // video duration text formatter
    private Formatter mFormatter;


    private IVideoPlayer mVideoPlayer;

    private IVisibilityChangedListener mVisibilityListener;
    private IControllerCallback.IVideoCallback mControllerVideoCallback;
    private IControllerCallback.IPlayerCallback mControllerPlayerCallback;
    private IControllerCallback.IBehaviorCallback mControllerBehaviorCallback;
    private IControllerCallback.IStatusCallback mControllerStatusCallback;

    private IVideoStatusListener mVideoStatusListener;


    private VideoTouchRoot mTouchRoot;
    // ---------- Video Action Buttons ----------
    private View mBtnPrev;
    private View mBtnNext;
    private ImageView mBtnPauseResume;
    private TextView mTvTimeCurrent;
    private SeekBar mSeekBar;
    private TextView mTvTimeTotal;

    // ---------- Player Action Buttons ----------
    private View mBtnScaleUp;
    private View mBtnScaleDown;
    private View mBtnClose;
    private View mBtnShare;
    private View mBtnFavorite;
    private View mBtnYouTubeIcon;

    // ---------- Behavior Action Buttons ----------
    private View mBtnRepeat;
    private View mProgressLoading;
    // --------------------------------------------------

    // There are two scenarios that can trigger the seekbar listener to trigger:
    //
    // 1. User using the touch panel to adjust the position of the seekbar's thumb.
    // In this case onStartTrackingTouch is called followed by
    // a number of onProgressChanged notifications, concluded by onStopTrackingTouch.
    // We're setting the field "mDragging" to true for the duration of the dragging
    // session to avoid jumps in the position in case of ongoing playback.
    //
    // 2. Involves the user operating the scroll ball.
    // In this case there WON'T BE onStartTrackingTouch/onStopTrackingTouch notifications,
    // we will simply apply the updated position without suspending regular updates.
    private final SeekBar.OnSeekBarChangeListener mOnSeekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            show(3600000);

            mDragging = true;

            // By removing these pending progress messages, we make sure that
            // (a) we won't update the progress while the user adjusts the seekbar and
            // (b) once the user is done dragging the thumb, we will post one of these messages to
            // the queue again and this ensures that there will be exactly one message queued up.
            mUiHandler.removeMessages(HandlerMsg.SHOW_PROGRESS);
        }

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (!fromUser) {
                // We're not interested in programmatically generated changes to the progress bar's position.
                return;
            }
            long duration = mVideoPlayer.getDuration();
            long newPosition = (duration * progress) / 1000L;
            mVideoPlayer.seekTo((int) newPosition);
            if (mTvTimeCurrent != null) {
                mTvTimeCurrent.setText(stringForTime((int) newPosition));
            }
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            mDragging = false;
            setProgress();
            updatePausePlay();
            show(DEFAULT_DISPLAY_TIMEOUT);

            // Ensure that progress is properly updated in the future, the call to show()
            // does not guarantee this because it is a  no-op if we are already showing.
            mUiHandler.sendEmptyMessage(HandlerMsg.SHOW_PROGRESS);
        }
    };


    private boolean mFirstTimeLoading = true;
    private boolean mShowing;
    private boolean mDragging;
    private int mLastPlayedSeconds = -1;


    // ---------- Constructors ----------

    public SkPlayerController(@NonNull Context context) {
        this(context, null);
    }

    public SkPlayerController(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SkPlayerController(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public SkPlayerController(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        if (isInEditMode()) {
            return;
        }
        init();
    }

    private void init() {
        initButtonAnimation();
        initStringFormatter();
    }

    private void initButtonAnimation() {
        mScaleAnimation = new ScaleAnimation(
                1f, 0.8f, // Start and end values for the X axis scaling
                1f, 0.8f, // Start and end values for the Y axis scaling
                Animation.RELATIVE_TO_SELF, 0.5f, // Pivot point of X scaling
                Animation.RELATIVE_TO_SELF, 0.5f); // Pivot point of Y scaling
        // scaleAnimation.setFillAfter(true); // Needed to keep the result of the animation
        mScaleAnimation.setDuration(100);
    }

    private void initStringFormatter() {
        mFormatBuilder = new StringBuilder();
        mFormatter = new Formatter(mFormatBuilder, Locale.getDefault());
    }


    // ---------- View functions override ----------

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

    private void bindView() {
        Context context = getContext();
        final LayoutInflater layoutInflater = context != null ? LayoutInflater.from(context) : null;
        if (layoutInflater == null) {
            return;
        }
        switch (mPlayerSize) {
            case SkVideoPlayerView.PlayerSize.MINIMUM:
                layoutInflater.inflate(R.layout.floating_player_controller_small, this);
                // ---------- Video Action Buttons ----------
                mBtnPrev = null;
                mBtnNext = null;
                mBtnPauseResume = (ImageView) findViewById(R.id.controller_center_btn_pause_resume);
                mTvTimeCurrent = null;
                mSeekBar = null;
                mTvTimeTotal = null;
                // ---------- Player Action Buttons ----------
                mBtnScaleUp = null;
                mBtnScaleDown = null;
                mBtnClose = findViewById(R.id.controller_center_btn_close);
                mBtnShare = null;
                mBtnFavorite = null;
                mBtnYouTubeIcon = null;
                // ---------- Behavior Action Buttons ----------
                mBtnRepeat = null;
                mProgressLoading = findViewById(R.id.controller_center_progress_loading);
                break;
            case SkVideoPlayerView.PlayerSize.MEDIUM:
                layoutInflater.inflate(R.layout.floating_player_controller_medium, this);
                // ---------- Video Action Buttons ----------
                mBtnPrev = findViewById(R.id.controller_center_btn_previous);
                mBtnNext = findViewById(R.id.controller_center_btn_next);
                mBtnPauseResume = (ImageView) findViewById(R.id.controller_center_btn_pause_resume);
                mTvTimeCurrent = (TextView) findViewById(R.id.controller_bottom_tv_time_current);
                mSeekBar = (SeekBar) findViewById(R.id.controller_bottom_seek_bar);
                mTvTimeTotal = (TextView) findViewById(R.id.controller_bottom_tv_time_total);
                // ---------- Player Action Buttons ----------
                mBtnScaleUp = findViewById(R.id.controller_top_btn_scale_up);
                mBtnScaleDown = findViewById(R.id.controller_top_btn_scale_down);
                mBtnClose = findViewById(R.id.controller_top_btn_close);
                mBtnShare = findViewById(R.id.controller_top_btn_share);
                mBtnFavorite = findViewById(R.id.controller_top_btn_favorite);
                mBtnYouTubeIcon = findViewById(R.id.controller_center_btn_youtube_icon);
                // ---------- Behavior Action Buttons ----------
                mBtnRepeat = findViewById(R.id.controller_top_btn_repeat);
                mProgressLoading = findViewById(R.id.controller_center_progress_loading);
                break;
            case SkVideoPlayerView.PlayerSize.FULLSCREEN:
                layoutInflater.inflate(R.layout.floating_player_controller_fullscreen, this);
                // ---------- Video Action Buttons ----------
                mBtnPrev = findViewById(R.id.controller_center_btn_previous);
                mBtnNext = findViewById(R.id.controller_center_btn_next);
                mBtnPauseResume = (ImageView) findViewById(R.id.controller_center_btn_pause_resume);
                mTvTimeCurrent = (TextView) findViewById(R.id.controller_bottom_tv_time_current);
                mSeekBar = (SeekBar) findViewById(R.id.controller_bottom_seek_bar);
                mTvTimeTotal = (TextView) findViewById(R.id.controller_bottom_tv_time_total);
                // ---------- Player Action Buttons ----------
                mBtnScaleUp = findViewById(R.id.controller_top_btn_scale_up);
                mBtnScaleDown = findViewById(R.id.controller_top_btn_scale_down);
                mBtnClose = findViewById(R.id.controller_top_btn_close);
                mBtnShare = findViewById(R.id.controller_top_btn_share);
                mBtnFavorite = findViewById(R.id.controller_top_btn_favorite);
                mBtnYouTubeIcon = findViewById(R.id.controller_center_btn_youtube_icon);
                // ---------- Behavior Action Buttons ----------
                mBtnRepeat = findViewById(R.id.controller_top_btn_repeat);
                mProgressLoading = findViewById(R.id.controller_center_progress_loading);
                break;
        }
    }

    private void bindListeners() {
        switch (mPlayerSize) {
            case SkVideoPlayerView.PlayerSize.MINIMUM:
                break;
            case SkVideoPlayerView.PlayerSize.MEDIUM:
            case SkVideoPlayerView.PlayerSize.FULLSCREEN:
                mSeekBar.setOnSeekBarChangeListener(mOnSeekBarChangeListener);
                break;
        }
        mTouchRoot = (VideoTouchRoot) findViewById(R.id.controller_touch_root);
        mTouchRoot.setOnTouchReceiver(this);
    }

    private void initControllerUI() {
//        boolean isRepeatOn = YouTubeUtils.getYoutubeRepeatMode().equals(YouTubeUtils.RepeatMode.Single);
//        mTopBtnRepeat.setTextColor(getResources().getColor(isRepeatOn ? R.color.gela_green_highlight : android.R.color.white));
        switch (mPlayerSize) {
            case SkVideoPlayerView.PlayerSize.MINIMUM:
                break;
            case SkVideoPlayerView.PlayerSize.MEDIUM:
            case SkVideoPlayerView.PlayerSize.FULLSCREEN:
                mSeekBar.setMax(1000);
                break;
        }
    }

    @Override
    public boolean onTrackballEvent(MotionEvent event) {
        show(DEFAULT_DISPLAY_TIMEOUT);
        return false;
    }

    @Override
    public void onInitializeAccessibilityEvent(AccessibilityEvent event) {
        super.onInitializeAccessibilityEvent(event);
        event.setClassName(SkPlayerController.class.getName());
    }

    @Override
    public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo info) {
        super.onInitializeAccessibilityNodeInfo(info);
        info.setClassName(SkPlayerController.class.getName());
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        int keyCode = event.getKeyCode();
        final boolean uniqueDown = event.getRepeatCount() == 0 && event.getAction() == KeyEvent.ACTION_DOWN;

        switch (keyCode) {
            case KeyEvent.KEYCODE_HEADSETHOOK:
            case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
            case KeyEvent.KEYCODE_SPACE:
                if (uniqueDown && mVideoPlayer != null) {
                    doPauseResume();
                    show(DEFAULT_DISPLAY_TIMEOUT);
                }
                return true;
            case KeyEvent.KEYCODE_MEDIA_PLAY:
                if (uniqueDown && mVideoPlayer != null && !mVideoPlayer.isPlaying()) {
                    mVideoPlayer.start();
                    updatePausePlay();
                    show(DEFAULT_DISPLAY_TIMEOUT);
                }
                return true;
            case KeyEvent.KEYCODE_MEDIA_STOP:
            case KeyEvent.KEYCODE_MEDIA_PAUSE:
                if (uniqueDown && mVideoPlayer != null && mVideoPlayer.isPlaying()) {
                    mVideoPlayer.pause();
                    updatePausePlay();
                    show(DEFAULT_DISPLAY_TIMEOUT);
                }
                return true;
            case KeyEvent.KEYCODE_VOLUME_DOWN:
            case KeyEvent.KEYCODE_VOLUME_UP:
            case KeyEvent.KEYCODE_VOLUME_MUTE:
            case KeyEvent.KEYCODE_CAMERA:
                // don't show the controls for volume adjustment
                return super.dispatchKeyEvent(event);
            case KeyEvent.KEYCODE_BACK:
            case KeyEvent.KEYCODE_MENU:
                if (uniqueDown) {
                    hide();
                }
                return true;
        }
        show(DEFAULT_DISPLAY_TIMEOUT);
        return super.dispatchKeyEvent(event);
    }


    // ---------- Controller View ----------

    private int setProgress() {
        if (mVideoPlayer == null || mDragging) {
            return 0;
        }

        int position = mVideoPlayer.getCurrentPosition();
        int duration = mVideoPlayer.getDuration();

        if (mSeekBar != null) {
            if (duration > 0) {
                long pos = 1000L * position / duration;    // use long to avoid overflow
                mSeekBar.setProgress((int) pos);
            }
            int percent = mVideoPlayer.getBufferPercentage();
            mSeekBar.setSecondaryProgress(percent * 10);
        }

        if (mTvTimeTotal != null) {
            mTvTimeTotal.setText(stringForTime(duration));
        }

        if (mTvTimeCurrent != null) {
            mTvTimeCurrent.setText(stringForTime(position));
        }

        final int playedSeconds = position / 1000;

        if (mLastPlayedSeconds != playedSeconds) {
            mLastPlayedSeconds = playedSeconds;
        }

        return position;
    }

    private void doPauseResume() {
        if (mVideoPlayer == null) {
            return;
        }
        if (mVideoPlayer.isPlaying()) {
            mVideoPlayer.pause();
        } else {
            mVideoPlayer.start();
        }
        updatePausePlay();
    }

    public void updatePausePlay() {
        if (mVideoPlayer == null) {
            return;
        }

        boolean isPlaying = mVideoPlayer.isPlaying();
        int btnIconRes = isPlaying ? R.drawable.kika_kikago_float_ic_pause : R.drawable.kika_kikago_float_ic_play;

        if (mBtnPauseResume != null) {
            mBtnPauseResume.setImageResource(btnIconRes);
        }
    }

    private String stringForTime(final int timeMillis) {
        int totalSeconds = timeMillis / 1000;
        int seconds = totalSeconds % 60;
        int minutes = (totalSeconds / 60) % 60;
        int hours = totalSeconds / 3600;
        mFormatBuilder.setLength(0);
        return (hours > 0) ? mFormatter.format("%d:%02d:%02d", hours, minutes, seconds).toString()
                : mFormatter.format("%02d:%02d", minutes, seconds).toString();
    }


    public void setPlayerSize(@SkVideoPlayerView.PlayerSize int playerSize) {
        this.mPlayerSize = playerSize;
        bindView();
        bindListeners();
        initControllerUI();
    }

    public void onClickedEvent(MotionEvent event) {
        if (event == null) {
            return;
        }
        float rawX = event.getRawX();
        float rawY = event.getRawY();
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

    private View getPossibleBtn(View[] buttons, float rawX, float rawY) {
        RectF btnRect;
        try {
            for (View btn : buttons) {
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


    private void performOnPause(View btn) {
        AnimationUtils.getIns().animate(btn, mScaleAnimation, new AnimationUtils.IAnimationEndCallback() {
            @Override
            public void onEnd(View view) {
                if (mVideoPlayer == null) {
                    return;
                }
                doPauseResume();
                show(DEFAULT_DISPLAY_TIMEOUT);
            }
        });
    }

    private void performOnPrev(View btn) {
        AnimationUtils.getIns().animate(btn, mScaleAnimation, new AnimationUtils.IAnimationEndCallback() {
            @Override
            public void onEnd(View view) {
                if (mControllerVideoCallback != null) {
                    mControllerVideoCallback.onPrev();
                }
            }
        });
    }

    private void performOnNext(View btn) {
        AnimationUtils.getIns().animate(btn, mScaleAnimation, new AnimationUtils.IAnimationEndCallback() {
            @Override
            public void onEnd(View view) {
                if (mControllerVideoCallback != null) {
                    mControllerVideoCallback.onNext();
                }
            }
        });
    }


    private void performOnScaleUp(View btn) {
        AnimationUtils.getIns().animate(btn, mScaleAnimation, new AnimationUtils.IAnimationEndCallback() {
            @Override
            public void onEnd(View view) {
                if (mControllerPlayerCallback != null) {
                    mControllerPlayerCallback.onScaleUp();
                }
            }
        });
    }

    private void performOnScaleDown(View btn) {
        AnimationUtils.getIns().animate(btn, mScaleAnimation, new AnimationUtils.IAnimationEndCallback() {
            @Override
            public void onEnd(View view) {
                if (mControllerPlayerCallback != null) {
                    mControllerPlayerCallback.onScaleDown();
                }
            }
        });
    }

    private void performOnClose(View btn) {
        AnimationUtils.getIns().animate(btn, mScaleAnimation, new AnimationUtils.IAnimationEndCallback() {
            @Override
            public void onEnd(View view) {
                if (mControllerPlayerCallback != null) {
                    mControllerPlayerCallback.onClose();
                }
            }
        });
    }

    private void performOnShare(View btn) {
        AnimationUtils.getIns().animate(btn, mScaleAnimation, new AnimationUtils.IAnimationEndCallback() {
            @Override
            public void onEnd(View view) {
                // TODO
//                if (!YouTubeUtils.getHasClickYoutubeShareIconNew()) {
//                    YouTubeUtils.setHasClickYoutubeShareIconNew(true);
//                    view.setVisibility(GONE);
//                }
                if (mControllerPlayerCallback != null) {
                    mControllerPlayerCallback.onShare();
                }
            }
        });
    }

    private void performOnFavorite(View btn) {
        AnimationUtils.getIns().animate(btn, mScaleAnimation, new AnimationUtils.IAnimationEndCallback() {
            @Override
            public void onEnd(View view) {
                if (mControllerPlayerCallback != null) {
                    mControllerPlayerCallback.onFavorite();
                }
            }
        });
    }

    private void performOnYouTubeIconClick() {
        if (mControllerPlayerCallback != null) {
            mControllerPlayerCallback.onYouTubeIconClick();
        }
    }


    private void performOnRepeat(View btn) {
        AnimationUtils.getIns().animate(btn, mScaleAnimation, new AnimationUtils.IAnimationEndCallback() {
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


    // ---------- IVideoPlayerController Callbacks Impl ----------

    @Override
    public void setMediaPlayer(IVideoPlayer videoPlayer) {
        this.mVideoPlayer = videoPlayer;
    }

    @Override
    public synchronized void show() {
        show(DEFAULT_DISPLAY_TIMEOUT);
    }

    @Override
    public synchronized void show(int timeInMilliSeconds) {
        if (LogUtil.DEBUG) {
            LogUtil.log(TAG, String.format("timeInMilliSeconds: %s ms", timeInMilliSeconds));
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

        // Cause of the progress bar to be updated even if mShowing was already true.
        // This happens, for example, if we're paused with the progress bar showing the user hits play.
        mUiHandler.sendEmptyMessage(HandlerMsg.SHOW_PROGRESS);

        Message msg = mUiHandler.obtainMessage(HandlerMsg.FADE_OUT);

        if (timeInMilliSeconds != 0) {
            mUiHandler.removeMessages(HandlerMsg.FADE_OUT);
            mUiHandler.sendMessageDelayed(msg, timeInMilliSeconds);
        }

        if (mVisibilityListener != null) {
            mVisibilityListener.onControlsVisibilityChange(true);
        }
    }

    @Override
    public synchronized void hide() {
        if (mShowing || getVisibility() == VISIBLE) {
            try {
                mUiHandler.removeMessages(HandlerMsg.SHOW_PROGRESS);
                setVisibility(View.INVISIBLE);
            } catch (final IllegalArgumentException e) {
                // MediaController, already removed
                if (LogUtil.DEBUG) {
                    LogUtil.printStackTrace(TAG, e.getMessage(), e);
                }
            }
            mShowing = false;
        }

        if (mVisibilityListener != null) {
            mVisibilityListener.onControlsVisibilityChange(false);
        }
    }

    @Override
    public synchronized void showLoadingView() {
        if (mProgressLoading != null) {
            mProgressLoading.setVisibility(VISIBLE);
        }
    }

    @Override
    public synchronized void hideLoadingView() {
        hide();
        if (mProgressLoading != null) {
            mProgressLoading.setVisibility(GONE);
        }
    }

    @Override
    public void setVisibilityListener(IVisibilityChangedListener visibilityListener) {
        this.mVisibilityListener = visibilityListener;
    }


    // ---------- IVideoStatusListener Callbacks Impl ----------

    @Override
    public void onFirstVideoFrameRendered() {
        // controlsRoot: mCenterArea
        mFirstTimeLoading = false;
    }

    @Override
    public void onPlay() {
        hideLoadingView();
        if (mControllerStatusCallback != null) {
            mControllerStatusCallback.onPlay();
        }
    }

    @Override
    public void onPause() {
        if (mControllerStatusCallback != null) {
            mControllerStatusCallback.onPause();
        }
    }

    @Override
    public void onBuffer() {
        showLoadingView();
    }

    @Override
    public boolean onStopWithExternalError(int position) {
        return false;
    }


    // ---------- VideoTouchRoot.OnTouchReceiver Callbacks Impl ----------

    @Override
    public void onControllerUiTouched() {
        if (LogUtil.DEBUG) {
            LogUtil.log(TAG, String.format("mShowing: %s", mShowing));
        }
        show();
    }


    // ---------- Controller Callbacks ----------

    public SkPlayerController setControllerVideoCallback(IControllerCallback.IVideoCallback callback) {
        this.mControllerVideoCallback = callback;
        return this;
    }

    public SkPlayerController setControllerPlayerCallback(IControllerCallback.IPlayerCallback callback) {
        this.mControllerPlayerCallback = callback;
        return this;
    }

    public SkPlayerController setControllerBehaviorCallback(IControllerCallback.IBehaviorCallback callback) {
        this.mControllerBehaviorCallback = callback;
        return this;
    }

    public SkPlayerController setControllerStatusCallback(IControllerCallback.IStatusCallback callback) {
        this.mControllerStatusCallback = callback;
        return this;
    }

    public interface IControllerCallback {
        interface IVideoCallback {
            void onPrev();

            void onNext();
        }

        interface IPlayerCallback {
            void onScaleUp();

            void onScaleDown();

            void onClose();

            void onShare();

            void onFavorite();

            void onYouTubeIconClick();
        }

        interface IBehaviorCallback {
            void onRepeatModeChanged(boolean isChecked);

            void onLockModeLocked();

            void onLockModeUnlocked();
        }

        interface IStatusCallback {
            void onPlay();

            void onPause();
        }
    }
}
