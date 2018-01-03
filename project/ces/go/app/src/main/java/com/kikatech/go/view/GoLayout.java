package com.kikatech.go.view;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.gifdecoder.GifDecoder;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.FitCenter;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.load.resource.gif.GifDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.GlideDrawableImageViewTarget;
import com.bumptech.glide.request.target.Target;
import com.kikatech.go.R;
import com.kikatech.go.dialogflow.model.ContactOptionList;
import com.kikatech.go.dialogflow.model.Option;
import com.kikatech.go.dialogflow.model.OptionList;
import com.kikatech.go.dialogflow.model.TtsText;
import com.kikatech.go.services.DialogFlowForegroundService;
import com.kikatech.go.ui.ResolutionUtil;
import com.kikatech.go.util.AppInfo;
import com.kikatech.go.util.LogUtil;
import com.kikatech.go.util.timer.CountingTimer;
import com.kikatech.go.view.widget.GoTextView;
import com.vdurmont.emoji.EmojiManager;

import jp.wasabeef.glide.transformations.CropCircleTransformation;

/**
 * @author SkeeterWang Created on 2017/11/10.
 */
public class GoLayout extends FrameLayout {
    private static final String TAG = "GoLayout";

    private static final long EACH_STATUS_MIN_STAY_MILLIS = 1000;

    public enum DisplayMode {
        SLEEP(R.color.standby_bg_color, ViewStatus.STAND_BY_SLEEP),
        AWAKE(R.color.awake_bg_color, ViewStatus.STAND_BY_AWAKE);

        int bgRes;
        ViewStatus defaultStatus;

        DisplayMode(int bgRes, ViewStatus defaultStatus) {
            this.bgRes = bgRes;
            this.defaultStatus = defaultStatus;
        }

        public int getBgRes() {
            return bgRes;
        }

        public ViewStatus getDefaultStatus() {
            return defaultStatus;
        }
    }

    public enum ViewStatus {
        STAND_BY_SLEEP(R.drawable.kika_vui_standby, R.drawable.gmap_kika_vui_standby),
        SLEEP_TO_AWAKE(R.drawable.kika_vui_trans_s_aw, R.drawable.gmap_kika_vui_trans_s_aw),
        STAND_BY_AWAKE(R.drawable.kika_vui_awake, R.drawable.gmap_kika_vui_awake),

        TTS(R.drawable.kika_vui_t, R.drawable.gmap_kika_vui_t),
        TTS_TO_LISTEN(R.drawable.kika_vui_trans_t_l, R.drawable.gmap_kika_vui_trans_t_l),
        LISTEN_1(R.drawable.kika_vui_l_l, R.drawable.gmap_kika_vui_l_l),
        LISTEN_2(R.drawable.kika_vui_l_r, R.drawable.gmap_kika_vui_l_r),
        ANALYZE(R.drawable.kika_vui_a, R.drawable.gmap_kika_vui_a),
        ANALYZE_TO_TTS(R.drawable.kika_vui_trans_a_t, R.drawable.gmap_kika_vui_trans_a_t);

        int normalRes, smallRes;

        ViewStatus(int bgRes, int res) {
            this.normalRes = bgRes;
            this.smallRes = res;
        }

        public int getNormalRes() {
            return normalRes;
        }

        public int getSmallRes() {
            return smallRes;
        }
    }

    private DisplayMode mCurrentMode = DisplayMode.SLEEP;
    private ViewStatus mCurrentStatus;

    private LayoutInflater mLayoutInflater;

    private View mTouchWakeUpPanel;

    private View mSpeakLayout;
    private ImageView mSpeakViewIcon;
    private GoTextView mSpeakViewText;

    private View mListenLayout;
    private GoTextView mListenView;

    private View mOptionsContactLayout;
    private LinearLayout mOptionsContactItemLayout;
    private ImageView mOptionsContactAvatar;
    private TextView mOptionsContactName;
    private ImageView mOptionsContactImIcon;

    private View mOptionsLayout;
    private LinearLayout mOptionsItemLayout;
    private ImageView mOptionsTitleIcon;
    private GoTextView mOptionsTitleText;
    private TextView mOptionsTitleEmojiView;

    private View mUsrInfoLayout;
    private ImageView mUsrInfoAvatar;
    private GoTextView mUsrInfoName;
    private TextView mUsrInfoImName;
    private ImageView mUsrInfoImIcon;

    private View mUsrMsgLayout;
    private ImageView mUsrMsgAvatar;
    private ImageView mUsrMsgImIcon;
    private TextView mUsrMsgName;
    private GoTextView mUsrMsgContent;

    private View mMsgSentLayout;
    private View mMsgSentFailedLayout;

    private View mSleepLayout;

    private View mStatusLayout;
    private ImageView mStatusAnimationView;
    private GlideDrawableImageViewTarget mRepeatTarget;
    private GlideDrawableImageViewTarget mNonRepeatTarget;


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
        if (isInEditMode()) {
            return;
        }
        bindView();
        bindListener();
        sleep();
    }

    private void bindView() {
        mLayoutInflater = LayoutInflater.from(getContext());
        mLayoutInflater.inflate(R.layout.go_layout, this);

        mTouchWakeUpPanel = findViewById(R.id.go_layout_touch_wake_up_panel);

        mSpeakLayout = findViewById(R.id.go_layout_speak);
        mSpeakViewIcon = (ImageView) findViewById(R.id.go_layout_speak_icon);
        mSpeakViewText = (GoTextView) findViewById(R.id.go_layout_speak_text);

        mListenLayout = findViewById(R.id.go_layout_listen);
        mListenView = (GoTextView) findViewById(R.id.go_layout_listen_text);

        mOptionsLayout = findViewById(R.id.go_layout_options);
        mOptionsTitleIcon = (ImageView) findViewById(R.id.go_layout_options_title_icon);
        mOptionsTitleText = (GoTextView) findViewById(R.id.go_layout_options_title_text);
        mOptionsItemLayout = (LinearLayout) findViewById(R.id.go_layout_options_item);
        mOptionsTitleEmojiView = (TextView) findViewById(R.id.go_layout_options_title_emoji_view);

        mOptionsContactLayout = findViewById(R.id.go_layout_options_contact);
        mOptionsContactItemLayout = (LinearLayout) findViewById(R.id.go_layout_options_contact_item);
        mOptionsContactAvatar = (ImageView) findViewById(R.id.go_layout_options_contact_avatar);
        mOptionsContactName = (TextView) findViewById(R.id.go_layout_options_contact_name);
        mOptionsContactImIcon = (ImageView) findViewById(R.id.go_layout_options_contact_im_icon);

        mUsrInfoLayout = findViewById(R.id.go_layout_usr_info);
        mUsrInfoAvatar = (ImageView) findViewById(R.id.go_layout_usr_info_avatar);
        mUsrInfoImIcon = (ImageView) findViewById(R.id.go_layout_usr_info_im_icon);
        mUsrInfoImName = (TextView) findViewById(R.id.go_layout_usr_info_im_name);
        mUsrInfoName = (GoTextView) findViewById(R.id.go_layout_usr_info_name);

        mUsrMsgLayout = findViewById(R.id.go_layout_usr_msg);
        mUsrMsgAvatar = (ImageView) findViewById(R.id.go_layout_usr_msg_avatar);
        mUsrMsgImIcon = (ImageView) findViewById(R.id.go_layout_usr_msg_im_icon);
        mUsrMsgName = (TextView) findViewById(R.id.go_layout_usr_msg_name);
        mUsrMsgContent = (GoTextView) findViewById(R.id.go_layout_usr_msg_content);

        mMsgSentLayout = findViewById(R.id.go_layout_msg_sent);

        mMsgSentFailedLayout = findViewById(R.id.go_layout_msg_sent_failed);

        mSleepLayout = findViewById(R.id.go_layout_sleep);

        mStatusLayout = findViewById(R.id.go_layout_status);
        mStatusAnimationView = (ImageView) findViewById(R.id.go_layout_status_img);
        mRepeatTarget = new GlideDrawableImageViewTarget(mStatusAnimationView, -1);
        mNonRepeatTarget = new GlideDrawableImageViewTarget(mStatusAnimationView, 1);
    }

    private void bindListener() {
        mTouchWakeUpPanel.setOnTouchListener(new FlexibleOnTouchListener(100, new FlexibleOnTouchListener.ITouchListener() {
            @Override
            public void onLongPress(View view, MotionEvent event) {
            }

            @Override
            public void onShortPress(View view, MotionEvent event) {
            }

            @Override
            public void onClick(View view, MotionEvent event) {
                DialogFlowForegroundService.processDialogFlowWakeUp();
            }

            @Override
            public void onDown(View view, MotionEvent event) {
            }

            @Override
            public void onMove(View view, MotionEvent event, long timeSpentFromStart) {
            }

            @Override
            public void onUp(View view, MotionEvent event, long timeSpentFromStart) {
            }
        }));
    }


    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (mCurrentMode != null) {
            setBackgroundResource(mCurrentMode.getBgRes());
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
            performLock();
        }

        @Override
        public void onTimeTick(long millis) {
            if (LogUtil.DEBUG && millis % 1000 == 0) {
                LogUtil.logv(TAG, String.format("onTimeTick: %s", (float) millis / 1000));
            }
        }

        @Override
        public void onTimeTickEnd() {
            performUnlock();
        }

        @Override
        public void onInterrupted(long stopMillis) {
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
            LogUtil.log(TAG, "lock");
        }
        mTimer.start();
    }

    public synchronized void unlock() {
        if (LogUtil.DEBUG) {
            LogUtil.log(TAG, "unlock");
        }
        // TODO: onStatusChanged(ViewStatus.LOADING);
        if (mTimer.isCounting()) {
            mTimer.stop();
        }
    }

    public synchronized boolean isViewLocking() {
        return isViewLocking;
    }


    public void setSleepLayoutIconRes(int iconRes) { // TODO: for ces demo requirement, remove later
        ((ImageView) findViewById(R.id.sleep_view_icon)).setImageResource(iconRes);
    }

    public void sleep() {
        DisplayMode targetMode = DisplayMode.SLEEP;
        onModeChanged(targetMode);
        adjustComponentsViewVisibility(null);
        mSleepLayout.setVisibility(VISIBLE);
        if (mModeChangedListener != null) {
            mModeChangedListener.onChanged(targetMode);
        }
    }

    public void wakeUp() {
        DisplayMode targetMode = DisplayMode.AWAKE;
        onModeChanged(targetMode);
        mSleepLayout.setVisibility(GONE);
        adjustComponentsViewVisibility(null);
        setOnTouchListener(null);
        if (mModeChangedListener != null) {
            mModeChangedListener.onChanged(targetMode);
        }
    }

    private synchronized void onModeChanged(final DisplayMode mode) {
        mCurrentMode = mode;
        loadStatusGif(mode.getDefaultStatus(), true, new IGifStatusListener() {
            @Override
            public void onStart() {
            }

            @Override
            public void onStop(Exception e) {
                setBackgroundResource(mode.getBgRes());
            }
        });
    }

    public synchronized void enableTouchWakeUpPanel() {
        mTouchWakeUpPanel.setVisibility(VISIBLE);
    }

    public synchronized void disableTouchWakeUpPanel() {
        mTouchWakeUpPanel.setVisibility(GONE);
    }

    public synchronized void onConnectionStatusChanged(boolean connected) {
        if (!connected) {
            disableTouchWakeUpPanel();
        } else {
            switch (mCurrentMode) {
                case AWAKE:
                    break;
                case SLEEP:
                    enableTouchWakeUpPanel();
                    break;
            }
        }
    }

    /**
     * display content spoken by tts service
     **/
    public synchronized void speak(final TtsText ttsText) {
        if (LogUtil.DEBUG) {
            LogUtil.log(TAG, "speak");
        }

        if (DisplayMode.SLEEP.equals(mCurrentMode)) {
            return;
        }

        lock();

        Glide.with(getContext().getApplicationContext())
                .load(ttsText.getIconRes())
                .dontTransform()
                .diskCacheStrategy(DiskCacheStrategy.RESULT)
                .into(mSpeakViewIcon);

        String uiText = ttsText.getUiText();
        mSpeakViewText.setText(uiText);
        DialogFlowForegroundService.processMsgChanged(uiText);

        adjustComponentsViewVisibility(mSpeakLayout);

        onStatusChanged(ViewStatus.TTS);
    }

    /**
     * display content spoken by user (voice input)
     **/
    public synchronized void listen(final String text, final boolean isFinished) {
        if (DisplayMode.SLEEP.equals(mCurrentMode)) {
            return;
        }

        lock();

        if (!isFinished) {
            mListenView.disableResize(mListenView.getMinTextSize());
            mListenView.setAlpha(0.5f);
        } else {
            if (LogUtil.DEBUG) {
                LogUtil.log(TAG, String.format("text: %1$s", text));
            }
            mListenView.enableResize();
            mListenView.setAlpha(1.0f);
        }
        mListenView.setText(text);

        adjustComponentsViewVisibility(mListenLayout);

        if (!isFinished) {
            onStatusChanged(ViewStatus.LISTEN_1);
        }
    }

    /**
     * display content spoken by tts service with option list
     **/
    public synchronized void displayOptions(final OptionList optionList, final IOnOptionSelectListener listener) {
        if (LogUtil.DEBUG) {
            LogUtil.log(TAG, "displayOptions");
        }

        if (DisplayMode.SLEEP.equals(mCurrentMode)) {
            return;
        }

        lock();

        adjustComponentsViewVisibility(mOptionsLayout);

        mOptionsItemLayout.removeAllViews();

        try {
            if (optionList != null && !optionList.isEmpty()) {
                Context context = getContext();

                Glide.with(getContext().getApplicationContext())
                        .load(optionList.getIconRes())
                        .dontTransform()
                        .diskCacheStrategy(DiskCacheStrategy.RESULT)
                        .into(mOptionsTitleIcon);

                String title = optionList.getTitle();
                boolean isOnlyEmoji = EmojiManager.isOnlyEmojis(title);
                if (LogUtil.DEBUG) {
                    LogUtil.log(TAG, String.format("title: %1$s, isOnlyEmoji: %2$s", title, isOnlyEmoji));
                }
                if (isOnlyEmoji) {
                    mOptionsTitleText.setText("Add emoji?");
                    mOptionsTitleEmojiView.setText(title);
                    mOptionsTitleEmojiView.setVisibility(VISIBLE);
                } else {
                    mOptionsTitleText.setText(title);
                    mOptionsTitleEmojiView.setVisibility(GONE);
                }
                if (!optionList.isDefaultList()) {
                    DialogFlowForegroundService.processMsgChanged(title);
                }

                int ITEM_MARGIN = ResolutionUtil.dp2px(context, 7);
                for (final Option option : optionList.getList()) {
                    View optionView = mLayoutInflater.inflate(R.layout.go_layout_options_item, null);
                    final GoTextView optionText = (GoTextView) optionView.findViewById(R.id.options_item_text);
                    mOptionsItemLayout.addView(optionView);
                    optionText.setText(option.getDisplayText());
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

        onStatusChanged(ViewStatus.TTS);
    }

    public synchronized void startOptionProgress(int bosDuration) {
        if (mOptionsItemLayout.getVisibility() == GONE || mOptionsItemLayout.getChildCount() == 0) {
            return;
        }
        View optionView = mOptionsItemLayout.getChildAt(0);
        final GoTextView optionText = (GoTextView) optionView.findViewById(R.id.options_item_text);
        final ProgressBar progress = (ProgressBar) optionView.findViewById(R.id.options_item_progress);

        progress.setVisibility(VISIBLE);
        ValueAnimator valueAnimator = ValueAnimator.ofInt(100, 0);
        valueAnimator.setDuration(bosDuration);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                int value = (int) valueAnimator.getAnimatedValue();
                progress.setProgress(value);
            }
        });
        valueAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                optionText.setTextColor(Color.WHITE);
            }

            @Override
            public void onAnimationCancel(Animator animation) {
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
            }
        });
        valueAnimator.start();
    }

    /**
     * display content spoken by tts service with contact option list
     **/
    public synchronized void displayContactOptions(final ContactOptionList optionList, final IOnOptionSelectListener listener) {
        if (LogUtil.DEBUG) {
            LogUtil.log(TAG, "displayOptions");
        }

        if (DisplayMode.SLEEP.equals(mCurrentMode)) {
            return;
        }

        lock();

        adjustComponentsViewVisibility(mOptionsContactLayout);

        mOptionsContactItemLayout.removeAllViews();

        try {
            if (optionList != null && !optionList.isEmpty()) {
                Context appCtx = getContext().getApplicationContext();

                AppInfo appInfo = optionList.getAppInfo();
                if (appInfo != null) {
                    Glide.with(appCtx)
                            .load(appInfo.getAppIconSmall())
                            .dontTransform()
                            .diskCacheStrategy(DiskCacheStrategy.RESULT)
                            .into(mOptionsContactImIcon);
                } else {
                    Glide.clear(mOptionsContactImIcon);
                }

                Glide.with(appCtx)
                        .load(optionList.getAvatar())
                        .asBitmap()
                        .error(R.drawable.kika_userpic_small_default)
                        .transform(new FitCenter(appCtx), new CropCircleTransformation(appCtx))
                        .diskCacheStrategy(DiskCacheStrategy.RESULT)
                        .into(mOptionsContactAvatar);

                String contactName = optionList.getTitle();
                mOptionsContactName.setText(contactName);
                DialogFlowForegroundService.processMsgChanged(contactName);

                int ITEM_MARGIN = ResolutionUtil.dp2px(appCtx, 7);
                for (final Option option : optionList.getList()) {
                    View optionView = mLayoutInflater.inflate(R.layout.go_layout_options_item, null);
                    final GoTextView optionText = (GoTextView) optionView.findViewById(R.id.options_item_text);
                    mOptionsContactItemLayout.addView(optionView);
                    optionText.setText(option.getDisplayText());
                    optionText.setOnClickListener(new OnClickListener() {
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

        onStatusChanged(ViewStatus.TTS);
    }

    public synchronized void displayUsrInfo(String usrAvatar, String usrName, AppInfo appInfo) {
        if (LogUtil.DEBUG) {
            LogUtil.log(TAG, "displayUsrInfo");
        }

        if (DisplayMode.SLEEP.equals(mCurrentMode)) {
            return;
        }

        lock();

        adjustComponentsViewVisibility(mUsrInfoLayout);

        Context context = getContext();
        Glide.with(context.getApplicationContext())
                .load(usrAvatar)
                .asBitmap()
                .error(R.drawable.kika_userpic_default)
                .transform(new FitCenter(context), new CropCircleTransformation(context))
                .diskCacheStrategy(DiskCacheStrategy.RESULT)
                .into(mUsrInfoAvatar);

        if (appInfo != null) {
            Glide.with(context.getApplicationContext())
                    .load(appInfo.getAppIconSmall())
                    .dontTransform()
                    .diskCacheStrategy(DiskCacheStrategy.RESULT)
                    .into(mUsrInfoImIcon);
            mUsrInfoImName.setText(appInfo.getAppName());
        } else {
            Glide.clear(mUsrInfoImIcon);
            mUsrInfoImName.setText("");
        }

        mUsrInfoName.setText(usrName);

        onStatusChanged(ViewStatus.TTS);
    }

    public synchronized void displayUsrMsg(String usrAvatar, String usrName, String msgContent, AppInfo appInfo) {
        if (LogUtil.DEBUG) {
            LogUtil.log(TAG, "displayUsrMsg");
        }

        if (DisplayMode.SLEEP.equals(mCurrentMode)) {
            return;
        }

        lock();

        adjustComponentsViewVisibility(mUsrMsgLayout);

        Context context = getContext();
        Glide.with(context.getApplicationContext())
                .load(usrAvatar)
                .asBitmap()
                .error(R.drawable.kika_userpic_small_default)
                .transform(new FitCenter(context), new CropCircleTransformation(context))
                .diskCacheStrategy(DiskCacheStrategy.RESULT)
                .into(mUsrMsgAvatar);

        if (appInfo != null) {
            Glide.with(context.getApplicationContext())
                    .load(appInfo.getAppIconSmall())
                    .dontTransform()
                    .diskCacheStrategy(DiskCacheStrategy.RESULT)
                    .into(mUsrMsgImIcon);
        } else {
            Glide.clear(mUsrMsgImIcon);
        }

        mUsrMsgName.setText(usrName);
        mUsrMsgContent.setText(msgContent);

        onStatusChanged(ViewStatus.TTS);
    }

    public synchronized void displayMsgSent(boolean success) {
        if (LogUtil.DEBUG) {
            LogUtil.log(TAG, "displayMsgSent");
        }

        if (DisplayMode.SLEEP.equals(mCurrentMode)) {
            return;
        }

        lock();

        adjustComponentsViewVisibility(success ? mMsgSentLayout : mMsgSentFailedLayout);

        onStatusChanged(ViewStatus.TTS);
    }

    private synchronized void adjustComponentsViewVisibility(View componentsToShow) {
        mSpeakLayout.setVisibility(GONE);
        mListenLayout.setVisibility(GONE);
        mOptionsLayout.setVisibility(GONE);
        mOptionsContactLayout.setVisibility(GONE);
        mUsrInfoLayout.setVisibility(GONE);
        mUsrMsgLayout.setVisibility(GONE);
        mMsgSentLayout.setVisibility(GONE);
        mMsgSentFailedLayout.setVisibility(GONE);
        if (componentsToShow != null) {
            componentsToShow.setVisibility(VISIBLE);
        }
    }


    public synchronized void onStatusChanged(final ViewStatus status) {
        onStatusChanged(status, null);
    }

    public synchronized void onStatusChanged(final ViewStatus status, IGifStatusListener listener) {
        ViewStatus nextStatus = getNextStatus(status);
        if (nextStatus == null) {
            return;
        }
        onNewStatus(nextStatus, listener);
    }

    private synchronized ViewStatus getNextStatus(ViewStatus status) {
        if (mCurrentMode == null) {
            return null;
        }
        ViewStatus nextStatus = null;
        switch (mCurrentMode) {
            case SLEEP:
                break;
            case AWAKE:
                if (mCurrentStatus == null) {
                    nextStatus = status;
                    break;
                } else if (mCurrentStatus.equals(status)) {
                    break;
                }
                switch (mCurrentStatus) {
                    case TTS:
                        switch (status) {
                            case LISTEN_1:
                                nextStatus = ViewStatus.TTS_TO_LISTEN;
                                break;
                            default:
                                nextStatus = status;
                                break;
                        }
                        break;
                    case LISTEN_1:
                    case LISTEN_2:
                        switch (status) {
                            case LISTEN_1:
                            case LISTEN_2:
                                break;
                            case TTS:
                                nextStatus = ViewStatus.ANALYZE_TO_TTS;
                                break;
                            default:
                                nextStatus = status;
                                break;
                        }
                        break;
                    case ANALYZE:
                        switch (status) {
                            case TTS:
                                nextStatus = ViewStatus.ANALYZE_TO_TTS;
                                break;
                            default:
                                nextStatus = status;
                                break;
                        }
                        break;
                    default:
                        nextStatus = status;
                        break;
                }
                break;
        }
        return nextStatus;
    }

    private synchronized void onNewStatus(ViewStatus status, final IGifStatusListener listener) {
        mCurrentStatus = status;
        boolean toRepeat = listener == null;
        switch (status) {
            case TTS:
                loadStatusGif(status, toRepeat, listener);
                break;
            case TTS_TO_LISTEN:
                loadStatusGif(status, false, new IGifStatusListener() {
                    @Override
                    public void onStart() {
                        if (listener != null) {
                            listener.onStart();
                        }
                    }

                    @Override
                    public void onStop(Exception e) {
                        if (listener != null) {
                            listener.onStop(e);
                        }
                        onNewStatus(ViewStatus.LISTEN_1, null);
                    }
                });
                break;
            case LISTEN_1:
                loadStatusGif(status, false, new GoLayout.IGifStatusListener() {
                    @Override
                    public void onStart() {
                        if (listener != null) {
                            listener.onStart();
                        }
                    }

                    @Override
                    public void onStop(Exception e) {
                        if (listener != null) {
                            listener.onStop(e);
                        }
                        onNewStatus(ViewStatus.LISTEN_2, null);
                    }
                });
                break;
            case LISTEN_2:
                loadStatusGif(status, false, new GoLayout.IGifStatusListener() {
                    @Override
                    public void onStart() {
                        if (listener != null) {
                            listener.onStart();
                        }
                    }

                    @Override
                    public void onStop(Exception e) {
                        if (listener != null) {
                            listener.onStop(e);
                        }
                        onNewStatus(ViewStatus.LISTEN_1, null);
                    }
                });
                break;
            case ANALYZE:
                loadStatusGif(ViewStatus.ANALYZE, toRepeat, listener);
                break;
            case ANALYZE_TO_TTS:
                loadStatusGif(ViewStatus.ANALYZE_TO_TTS, false, new GoLayout.IGifStatusListener() {
                    @Override
                    public void onStart() {
                        if (listener != null) {
                            listener.onStart();
                        }
                    }

                    @Override
                    public void onStop(Exception e) {
                        if (listener != null) {
                            listener.onStop(e);
                        }
                        onNewStatus(ViewStatus.TTS, null);
                    }
                });
                break;
        }
    }

    private Handler mUIHandler = new Handler(Looper.getMainLooper());

    private synchronized void loadStatusGif(final ViewStatus status, final boolean toRepeat, final IGifStatusListener listener) {
        if (LogUtil.DEBUG) {
            LogUtil.log(TAG, "handleStatusChanged: status: " + status.name());
        }
        DialogFlowForegroundService.processStatusChanged(status);
        mUIHandler.removeCallbacksAndMessages(null);
        mUIHandler.post(new Runnable() {
            @Override
            public void run() {
                Context context = getContext();

                if (listener != null) {
                    listener.onStart();
                }

                Glide.with(context.getApplicationContext())
                        .load(status.getNormalRes())
                        .placeholder(R.drawable.kika_emptypic)
                        .dontTransform()
                        .dontAnimate()
                        .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                        .listener(new RequestListener<Integer, GlideDrawable>() {
                            @Override
                            public boolean onException(Exception e, Integer model, Target<GlideDrawable> target, boolean isFirstResource) {
                                if (listener != null) {
                                    listener.onStop(e);
                                }
                                return false;
                            }

                            @Override
                            public boolean onResourceReady(GlideDrawable resource, Integer model, Target<GlideDrawable> target,
                                                           boolean isFromMemoryCache, boolean isFirstResource) {
                                if (listener != null) {
                                    if (resource instanceof GifDrawable) {
                                        // 计算动画时长
                                        int duration = getGifDuration((GifDrawable) resource);
                                        mUIHandler.postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                //发送延时消息，通知动画结束
                                                // handler.sendEmptyMessageDelayed(MESSAGE_SUCCESS,duration);
                                                listener.onStop(null);
                                            }
                                        }, duration);
                                    }
                                }
                                return false;
                            }
                        }) //仅仅加载一次gif动画
                        .into(toRepeat ? mRepeatTarget : mNonRepeatTarget);
            }
        });
    }

    private int getGifDuration(GifDrawable drawable) {
        int duration = 0;
        GifDecoder decoder = drawable.getDecoder();
        for (int i = 0; i < drawable.getFrameCount(); i++) {
            duration += decoder.getDelay(i);
        }
        return duration;
    }

    public void clear() {
        mUIHandler.removeCallbacksAndMessages(null);
        Glide.clear(mRepeatTarget);
        Glide.clear(mNonRepeatTarget);
    }


    public DisplayMode getCurrentMode() {
        return mCurrentMode;
    }

    public ViewStatus getCurrentStatus() {
        return mCurrentStatus;
    }


    public interface IGifStatusListener {
        void onStart();

        void onStop(Exception e);
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
