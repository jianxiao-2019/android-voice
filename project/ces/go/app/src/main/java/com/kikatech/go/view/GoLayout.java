package com.kikatech.go.view;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.kikatech.go.BuildConfig;
import com.kikatech.go.R;
import com.kikatech.go.dialogflow.model.Option;
import com.kikatech.go.dialogflow.model.OptionList;
import com.kikatech.go.navigation.NavigationService;
import com.kikatech.go.ui.ResolutionUtil;
import com.kikatech.go.util.CountingTimer;
import com.kikatech.go.util.LogUtil;
import com.kikatech.go.view.widget.GoTextView;

/**
 * @author SkeeterWang Created on 2017/11/10.
 */
public class GoLayout extends FrameLayout {
    private static final String TAG = "GoLayout";

    private static final boolean DEBUG = BuildConfig.DEBUG;

    private static final long EACH_STATUS_MIN_STAY_MILLIS = 1500;

    public enum DisplayMode {
        SLEEP, AWAKE
    }

    public enum ViewStatus {
        SPEAK(R.drawable.tts, R.drawable.gmap_tts),
        LISTEN(R.drawable.listening, R.drawable.gmap_listening),
        LOADING(R.drawable.listening, R.drawable.gmap_listening);

        int bgRes, res;

        ViewStatus(int bgRes, int res) {
            this.bgRes = bgRes;
            this.res = res;
        }

        public int getBgRes() {
            return bgRes;
        }

        public int getRes() {
            return res;
        }
    }

    private DisplayMode mCurrentMode = DisplayMode.SLEEP;
    private ViewStatus mCurrentStatus;

    private LayoutInflater mLayoutInflater;

    private View mSpeakLayout;
    private GoTextView mSpeakView;
    private View mListenLayout;
    private GoTextView mListenView;
    private View mOptionsLayout;
    private LinearLayout mOptionsItemLayout;
    private GoTextView mOptionsTitle;
    private View mSleepLayout;

    private View mStatusLayout;
    private ImageView mStatusAnimationView;

    private TextView mDebugLogView;


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
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        bindView();
    }

    private void bindView() {
        mLayoutInflater = LayoutInflater.from(getContext());
        mLayoutInflater.inflate(DEBUG ? R.layout.go_layout_debug : R.layout.go_layout, this);

        mSpeakLayout = findViewById(R.id.go_layout_speak);
        mSpeakView = (GoTextView) findViewById(R.id.go_layout_speak_text);

        mListenLayout = findViewById(R.id.go_layout_listen);
        mListenView = (GoTextView) findViewById(R.id.go_layout_listen_text);

        mOptionsLayout = findViewById(R.id.go_layout_options);
        mOptionsTitle = (GoTextView) findViewById(R.id.go_layout_options_title);
        mOptionsItemLayout = (LinearLayout) findViewById(R.id.go_layout_options_item);

        mSleepLayout = findViewById(R.id.go_layout_sleep);

        mStatusLayout = findViewById(R.id.go_layout_status);
        mStatusAnimationView = (ImageView) findViewById(R.id.go_layout_status_img);

        if( DEBUG ) {
            mDebugLogView = (TextView) findViewById(R.id.go_layout_debug_log);
        }
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

    private IOnModeChangedListener mModeChangedListener;

    public void setOnModeChangedListener(IOnModeChangedListener listener) {
        this.mModeChangedListener = listener;
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

    public synchronized boolean isViewLocking() {
        return isViewLocking;
    }


    public void sleep() {
        onModeChanged(DisplayMode.SLEEP);
        mSpeakLayout.setVisibility(GONE);
        mListenLayout.setVisibility(GONE);
        mOptionsLayout.setVisibility(GONE);
        mSleepLayout.setVisibility(VISIBLE);
        Glide.with(getContext())
                .load(R.drawable.awake_normal)
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .into(mStatusAnimationView);
        mStatusAnimationView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                awake();
            }
        });
        if (mModeChangedListener != null) {
            mModeChangedListener.onChanged(DisplayMode.SLEEP);
        }
    }

    public void awake() {
        onModeChanged(DisplayMode.AWAKE);
        mSleepLayout.setVisibility(GONE);
        Glide.with(getContext())
                .load(R.drawable.standby)
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .into(mStatusAnimationView);
        mStatusAnimationView.setOnClickListener(null);
        if (mModeChangedListener != null) {
            mModeChangedListener.onChanged(DisplayMode.AWAKE);
        }
    }

    private void onModeChanged(DisplayMode mode) {
        mCurrentMode = mode;
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
    public synchronized void listen(final String text, final boolean isFinished) {
        if (LogUtil.DEBUG) {
            LogUtil.log(TAG, "listen");
        }
        lock();

        mCurrentStatus = ViewStatus.LISTEN;

        if (!isFinished) {
            mListenView.disableResize(mListenView.getMinTextSize());
            mListenView.setAlpha(0.5f);
        } else {
            mListenView.enableResize();
            mListenView.setAlpha(1.0f);
        }
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

        mCurrentStatus = ViewStatus.SPEAK;

        mSpeakLayout.setVisibility(GONE);
        mListenLayout.setVisibility(GONE);
        mOptionsLayout.setVisibility(VISIBLE);

        mOptionsItemLayout.removeAllViews();

        try {
            if (optionList != null && !optionList.isEmpty()) {
                mOptionsTitle.setText(optionList.getTitle());

                Context context = getContext();

                int itemRes = R.layout.go_layout_options_item_2;
                int ITEM_MARGIN = 0;

                if (optionList.size() == 2) {
                    ITEM_MARGIN = ResolutionUtil.dp2px(context, 7);

                } else if (optionList.size() == 3) {
                    itemRes = R.layout.go_layout_options_item_3;
                    ITEM_MARGIN = ResolutionUtil.dp2px(context, 9);
                }

                for (final Option option : optionList.getList()) {
                    GoTextView optionView = (GoTextView) mLayoutInflater.inflate(itemRes, null);
                    mOptionsItemLayout.addView(optionView);
                    optionView.setText(option.getDisplayText());
                    optionView.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (listener != null) {
                                listener.onSelected(optionList.getRequestType(), optionList.indexOf(option), option);
                            }
                        }
                    });
                    int topMargin = ITEM_MARGIN / 2;
                    int bottomMargin = ITEM_MARGIN / 2;
                    int idxOption = optionList.indexOf(option);
                    if (idxOption == 0) {
                        topMargin = 0;
                    } else if (idxOption == optionList.size() - 1) {
                        bottomMargin = 0;
                    }
                    LinearLayout.LayoutParams optionParam = (LinearLayout.LayoutParams) optionView.getLayoutParams();
                    optionParam.setMargins(0, topMargin, 0, bottomMargin);
                }
            }
        } catch (Exception e) {
            if (LogUtil.DEBUG) {
                LogUtil.printStackTrace(TAG, e.getMessage(), e);
            }
        }

        onStatusChanged(mCurrentStatus);
    }


    private void onStatusChanged(ViewStatus status) {
        // TODO: animations
        Context context = getContext();
        switch (mCurrentMode) {
            case AWAKE:
                Glide.with(context)
                        .load(status.getBgRes())
                        .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                        .into(mStatusAnimationView);
                NavigationService.processStatusChanged(context, status);
//                switch (status) {
//                    case LOADING:
//                        break;
//                    case SPEAK:
//                        break;
//                    case LISTEN:
//                        break;
//                }
                break;
            case SLEEP:
                break;
        }
    }


    public ViewStatus getCurrentStatus() {
        return mCurrentStatus;
    }

    public void writeDebugInfo(String info) {
        if (DEBUG && mDebugLogView != null) {
            if (!TextUtils.isEmpty(info)) {
                mDebugLogView.setText(info);
                mDebugLogView.setVisibility(VISIBLE);
            } else {
                mDebugLogView.setVisibility(GONE);
            }
        }
    }


    public interface IOnModeChangedListener {
        void onChanged(DisplayMode mode);
    }

    public interface IOnOptionSelectListener {
        void onSelected(byte requestType, int index, Option option);
    }

    public interface IOnLockStateChangeListener {
        void onLocked();

        void onLockReleased();
    }
}
