package com.kikatech.go.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.kikatech.go.R;
import com.kikatech.go.dialogflow.model.Option;
import com.kikatech.go.dialogflow.model.OptionList;
import com.kikatech.go.ui.ResolutionUtil;
import com.kikatech.go.util.CountingTimer;
import com.kikatech.go.util.LogUtil;
import com.kikatech.go.view.widget.GoTextView;

/**
 * @author SkeeterWang Created on 2017/11/10.
 */
public class GoLayout extends FrameLayout {
    private static final String TAG = "GoLayout";

    private static final long EACH_STATUS_MIN_STAY_MILLIS = 1500;

    private enum DisplayMode {
        SLEEP, AWAKE
    }

    private enum ViewStatus {
        SPEAK, LISTEN, DISPLAY_OPTIONS, LOADING
    }

    private DisplayMode mCurrentMode = DisplayMode.AWAKE;
    private ViewStatus mCurrentStatus;

    private LayoutInflater mLayoutInflater;

    private View mSpeakLayout;
    private GoTextView mSpeakView;
    private View mListenLayout;
    private GoTextView mListenView;
    private LinearLayout mOptionsLayout;
    private GoTextView mOptionsTitle;

    private View mStatusLayout;
    private TextView mStatusAnimationView;


    public GoLayout(Context context) {
        this(context, null);
    }

    public GoLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public GoLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public GoLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        bindView();
    }

    private void bindView() {
        mLayoutInflater = LayoutInflater.from(getContext());

        mLayoutInflater.inflate(R.layout.go_layout, this);

        mSpeakLayout = findViewById(R.id.go_layout_speak);
        mSpeakView = (GoTextView) findViewById(R.id.go_layout_speak_text);

        mListenLayout = findViewById(R.id.go_layout_listen);
        mListenView = (GoTextView) findViewById(R.id.go_layout_listen_text);

        mOptionsLayout = (LinearLayout) findViewById(R.id.go_layout_options);
        mOptionsTitle = (GoTextView) mLayoutInflater.inflate(R.layout.go_layout_option_title, null);

        mStatusLayout = findViewById(R.id.go_layout_status);
        mStatusAnimationView = (TextView) findViewById(R.id.go_layout_status_text);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        switch (mCurrentMode) {
            case SLEEP:
                setBackgroundResource(R.drawable.kika_awake_bg);
                break;
            case AWAKE:
                setBackgroundResource(R.drawable.kika_normal_bg);
                break;
        }
    }


    private IOnLockStateChangeListener mLockStateChangeListener;

    public void setOnLockStateChangeListener(IOnLockStateChangeListener listener) {
        mLockStateChangeListener = listener;
    }

    private boolean isViewLocking;

    private CountingTimer mTimer = new CountingTimer(EACH_STATUS_MIN_STAY_MILLIS, 100, new CountingTimer.ICountingListener() {
        @Override
        public void onTimeTickStart() {
            if (LogUtil.DEBUG) {
                LogUtil.logv(TAG, "onTimeTickStart");
            }
            performLock();
        }

        @Override
        public void onTimeTick(long millis) {
            if (LogUtil.DEBUG && millis % 1000 == 0) {
                LogUtil.logv(TAG, "onTimeTick: " + millis);
            }
        }

        @Override
        public void onTimeTickEnd() {
            if (LogUtil.DEBUG) {
                LogUtil.logv(TAG, "onTimeTickEnd");
            }
            performUnlock();
        }

        @Override
        public void onInterrupted(long stopMillis) {
            if (LogUtil.DEBUG) {
                LogUtil.logw(TAG, "onInterrupted, stopMillis: " + stopMillis);
            }
            performUnlock();
        }

        private void performLock() {
            isViewLocking = true;
            if (mLockStateChangeListener != null) {
                post(new Runnable() {
                    @Override
                    public void run() {
                        mLockStateChangeListener.onLocked();
                    }
                });
            }
        }

        private void performUnlock() {
            isViewLocking = false;
            if (mLockStateChangeListener != null) {
                post(new Runnable() {
                    @Override
                    public void run() {
                        mLockStateChangeListener.onLockReleased();
                    }
                });
            }
        }
    });

    private synchronized void lock() {
        if (LogUtil.DEBUG) {
            LogUtil.logv(TAG, "lock");
        }
        mTimer.start();
    }

    public synchronized void unlock() {
        if (LogUtil.DEBUG) {
            LogUtil.logv(TAG, "unlock");
        }
        onStatusChanged(ViewStatus.LOADING);
        if (mTimer.isCounting()) {
            mTimer.stop();
        }
    }

    public boolean isViewLocking() {
        return isViewLocking;
    }


    public void onModeChanged(DisplayMode mode) {
        mCurrentMode = (mCurrentMode.equals(DisplayMode.SLEEP)) ? DisplayMode.AWAKE : DisplayMode.SLEEP;
        requestLayout();
    }


    /**
     * display content spoken by tts service
     **/
    public synchronized void speak(final String text) {
        if (LogUtil.DEBUG) {
            LogUtil.log(TAG, "speak");
        }
        lock();

        mCurrentStatus = ViewStatus.SPEAK;

        mSpeakView.setText(text);

        mSpeakLayout.setVisibility(VISIBLE);
        mListenLayout.setVisibility(GONE);
        mOptionsLayout.setVisibility(GONE);

        onStatusChanged(mCurrentStatus);
    }

    /**
     * display content spoken by user (voice input)
     **/
    public synchronized void listen(final String text) {
        if (LogUtil.DEBUG) {
            LogUtil.log(TAG, "listen");
        }
        lock();

        mCurrentStatus = ViewStatus.LISTEN;

        mListenView.setText(text);

        mSpeakLayout.setVisibility(GONE);
        mListenLayout.setVisibility(VISIBLE);
        mOptionsLayout.setVisibility(GONE);

        onStatusChanged(mCurrentStatus);
    }

    /**
     * display content spoken by tts service with option list
     **/
    public synchronized void displayOptions(final OptionList optionList, final IOnOptionSelectListener listener) {
        if (LogUtil.DEBUG) {
            LogUtil.log(TAG, "displayOptions");
        }
        lock();

        mCurrentStatus = ViewStatus.DISPLAY_OPTIONS;

        mSpeakLayout.setVisibility(GONE);
        mListenLayout.setVisibility(GONE);
        mOptionsLayout.setVisibility(VISIBLE);

        mOptionsLayout.removeAllViews();

        try {
            if (optionList != null && !optionList.isEmpty()) {
                mOptionsTitle.setText(optionList.getTitle());
                mOptionsLayout.addView(mOptionsTitle);
                for (final Option option : optionList.getList()) {
                    GoTextView optionView = (GoTextView) mLayoutInflater.inflate(R.layout.go_layout_option_item, null);
                    mOptionsLayout.addView(optionView);
                    optionView.setText(option.getDisplayText());
                    optionView.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (listener != null) {
                                listener.onSelected(optionList.getRequestType(), optionList.indexOf(option), option);
                            }
                        }
                    });
                }
                resolveOptionLayoutMargin();
            }
        } catch (Exception e) {
            if (LogUtil.DEBUG) {
                LogUtil.printStackTrace(TAG, e.getMessage(), e);
            }
        }

        onStatusChanged(mCurrentStatus);
    }

    private void resolveOptionLayoutMargin() {
        try {
            final Context context = getContext();
            final int CHILD_COUNT = mOptionsLayout.getChildCount();
            final int DEFAULT_TITLE_MARGIN_BOTTOM = ResolutionUtil.dp2px(context,10);
            final int DEFAULT_TOTAL_MARGIN = ResolutionUtil.dp2px(context, 30);
            final int ITEM_MARGIN_TOP = DEFAULT_TOTAL_MARGIN / (CHILD_COUNT - 1);
            LinearLayout.LayoutParams titleParam = (LinearLayout.LayoutParams) mOptionsTitle.getLayoutParams();
            titleParam.setMargins(0, 0, 0, DEFAULT_TITLE_MARGIN_BOTTOM);
            mOptionsTitle.setLayoutParams(titleParam);
            for (int i = 1; i < CHILD_COUNT; i++) {
                View child = mOptionsLayout.getChildAt(i);
                LinearLayout.LayoutParams optionParam = (LinearLayout.LayoutParams) child.getLayoutParams();
                optionParam.setMargins(0, ITEM_MARGIN_TOP, 0, 0);
                child.setLayoutParams(optionParam);
            }
        } catch (Exception e) {
            if (LogUtil.DEBUG) {
                LogUtil.printStackTrace(TAG, e.getMessage(), e);
            }
        }
    }

    private void onStatusChanged(ViewStatus status) {
        // TODO: animations
        switch (status) {
            case LOADING:
                mStatusAnimationView.setBackgroundResource(R.drawable.bg_transparent_round_blue);
                mStatusAnimationView.setTextColor(getResources().getColor(android.R.color.holo_blue_dark));
                mStatusAnimationView.setText("请说出指令");
                break;
            case DISPLAY_OPTIONS:
            case SPEAK:
                mStatusAnimationView.setBackgroundResource(R.drawable.bg_transparent_round_red);
                mStatusAnimationView.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                mStatusAnimationView.setText("TTS播放中");
                break;
            case LISTEN:
                mStatusAnimationView.setBackgroundResource(R.drawable.bg_transparent_round_green);
                mStatusAnimationView.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
                mStatusAnimationView.setText("指令辨识中");
                break;
        }
    }


    public interface IOnOptionSelectListener {
        void onSelected(byte requestType, int index, Option option);
    }

    public interface IOnLockStateChangeListener {
        void onLocked();

        void onLockReleased();
    }
}
