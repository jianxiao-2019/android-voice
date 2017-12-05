package com.kikatech.go.view;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import com.kikatech.go.BuildConfig;
import com.kikatech.go.R;
import com.kikatech.go.dialogflow.model.Option;
import com.kikatech.go.dialogflow.model.OptionList;
import com.kikatech.go.services.DialogFlowForegroundService;
import com.kikatech.go.ui.ResolutionUtil;
import com.kikatech.go.util.AppInfo;
import com.kikatech.go.util.CountingTimer;
import com.kikatech.go.util.LogUtil;
import com.kikatech.go.view.widget.GoTextView;

import jp.wasabeef.glide.transformations.CropCircleTransformation;

/**
 * @author SkeeterWang Created on 2017/11/10.
 */
public class GoLayout extends FrameLayout {
    private static final String TAG = "GoLayout";

    private static final boolean DEBUG = BuildConfig.DEBUG;

    private static final long EACH_STATUS_MIN_STAY_MILLIS = 1500;

    public enum DisplayMode {
        SLEEP(R.drawable.kika_awake_bg, ViewStatus.STAND_BY_SLEEP),
        AWAKE(R.drawable.kika_normal_bg, ViewStatus.STAND_BY_AWAKE);

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
        STAND_BY_SLEEP(R.drawable.kika_vui_standby, R.drawable.kika_gmap_standby),
        SLEEP_TO_AWAKE(R.drawable.kika_vui_trans_s_aw, R.drawable.kika_gmap_standby),
        STAND_BY_AWAKE(R.drawable.kika_vui_awake, R.drawable.kika_gmap_awake),

        TTS(R.drawable.kika_vui_t, R.drawable.kika_gmap_tts),
        TTS_TO_LISTEN(R.drawable.kika_vui_trans_t_l, R.drawable.kika_gmap_tts),
        LISTEN_1(R.drawable.kika_vui_l_l, R.drawable.kika_gmap_listening),
        LISTEN_2(R.drawable.kika_vui_l_r, R.drawable.kika_gmap_listening),
        ANALYZE(R.drawable.kika_vui_a, R.drawable.kika_listening),
        ANALYZE_TO_TTS(R.drawable.kika_vui_trans_a_t, R.drawable.kika_gmap_listening);

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

    private View mSpeakLayout;
    private GoTextView mSpeakView;

    private View mListenLayout;
    private GoTextView mListenView;

    private View mOptionsLayout;
    private LinearLayout mOptionsItemLayout;
    private GoTextView mOptionsTitle;

    private View mUsrInfoLayout;
    private ImageView mUsrInfoAvatar;
    private GoTextView mUsrInfoName;
    private ImageView mUsrInfoImIcon;

    private View mUsrMsgLayout;
    private ImageView mUsrMsgAvatar;
    private ImageView mUsrMsgImIcon;
    private TextView mUsrMsgName;
    private GoTextView mUsrMsgContent;

    private View mMsgSentLayout;

    private View mSleepLayout;

    private View mStatusLayout;
    private ImageView mStatusAnimationView;
    private GlideDrawableImageViewTarget mRepeatTarget;
    private GlideDrawableImageViewTarget mNonRepeatTarget;

    private TextView mDebugVersionView;
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

        mUsrInfoLayout = findViewById(R.id.go_layout_usr_info);
        mUsrInfoAvatar = (ImageView) findViewById(R.id.go_layout_usr_info_avatar);
        mUsrInfoImIcon = (ImageView) findViewById(R.id.go_layout_usr_info_im_icon);
        mUsrInfoName = (GoTextView) findViewById(R.id.go_layout_usr_info_name);

        mUsrMsgLayout = findViewById(R.id.go_layout_usr_msg);
        mUsrMsgAvatar = (ImageView) findViewById(R.id.go_layout_usr_msg_avatar);
        mUsrMsgImIcon = (ImageView) findViewById(R.id.go_layout_usr_msg_im_icon);
        mUsrMsgName = (TextView) findViewById(R.id.go_layout_usr_msg_name);
        mUsrMsgContent = (GoTextView) findViewById(R.id.go_layout_usr_msg_content);

        mMsgSentLayout = findViewById(R.id.go_layout_msg_sent);

        mSleepLayout = findViewById(R.id.go_layout_sleep);

        mStatusLayout = findViewById(R.id.go_layout_status);
        mStatusAnimationView = (ImageView) findViewById(R.id.go_layout_status_img);
        mRepeatTarget = new GlideDrawableImageViewTarget(mStatusAnimationView, -1);
        mNonRepeatTarget = new GlideDrawableImageViewTarget(mStatusAnimationView, 1);

        if (DEBUG) {
            mDebugVersionView = (TextView) findViewById(R.id.go_layout_debug_version);
            mDebugLogView = (TextView) findViewById(R.id.go_layout_debug_log);
            mDebugVersionView.setText(BuildConfig.VERSION_NAME);
        }
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


    public void sleep() {
        DisplayMode targetMode = DisplayMode.SLEEP;
        onModeChanged(targetMode);
        mSpeakLayout.setVisibility(GONE);
        mListenLayout.setVisibility(GONE);
        mOptionsLayout.setVisibility(GONE);
        mUsrInfoLayout.setVisibility(GONE);
        mUsrMsgLayout.setVisibility(GONE);
        mMsgSentLayout.setVisibility(GONE);
        mSleepLayout.setVisibility(VISIBLE);
        mStatusAnimationView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                awake();
            }
        });
        if (mModeChangedListener != null) {
            mModeChangedListener.onChanged(targetMode);
        }
    }

    public void awake() {
        DisplayMode targetMode = DisplayMode.AWAKE;
        onModeChanged(targetMode);
        mSleepLayout.setVisibility(GONE);
        mSpeakLayout.setVisibility(GONE);
        mListenLayout.setVisibility(GONE);
        mOptionsLayout.setVisibility(GONE);
        mUsrInfoLayout.setVisibility(GONE);
        mUsrMsgLayout.setVisibility(GONE);
        mMsgSentLayout.setVisibility(GONE);
        mStatusAnimationView.setOnClickListener(null);
        if (mModeChangedListener != null) {
            mModeChangedListener.onChanged(targetMode);
        }
    }

    private void onModeChanged(DisplayMode mode) {
        mCurrentMode = mode;
        requestLayout();
        Glide.with(getContext())
                .load(mode.getDefaultStatus().getNormalRes())
                .dontTransform()
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .into(mStatusAnimationView);
    }


    /**
     * display content spoken by tts service
     **/
    public synchronized void speak(final String text) {
        if (LogUtil.DEBUG) {
            LogUtil.log(TAG, "speak");
        }
        lock();

        mSpeakView.setText(text);

        mSpeakLayout.setVisibility(VISIBLE);
        mListenLayout.setVisibility(GONE);
        mOptionsLayout.setVisibility(GONE);
        mUsrInfoLayout.setVisibility(GONE);
        mUsrMsgLayout.setVisibility(GONE);
        mMsgSentLayout.setVisibility(GONE);

        onStatusChanged(ViewStatus.TTS);
    }

    /**
     * display content spoken by user (voice input)
     **/
    public synchronized void listen(final String text, final boolean isFinished) {
        if (LogUtil.DEBUG) {
            LogUtil.log(TAG, String.format("text: %1$s, isFinished: %2$s", text, isFinished));
        }
        lock();

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
        mUsrInfoLayout.setVisibility(GONE);
        mUsrMsgLayout.setVisibility(GONE);
        mMsgSentLayout.setVisibility(GONE);

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
        lock();

        mSpeakLayout.setVisibility(GONE);
        mListenLayout.setVisibility(GONE);
        mOptionsLayout.setVisibility(VISIBLE);
        mUsrInfoLayout.setVisibility(GONE);
        mUsrMsgLayout.setVisibility(GONE);
        mMsgSentLayout.setVisibility(GONE);

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

        onStatusChanged(ViewStatus.TTS);
    }

    public synchronized void displayUsrInfo(String usrAvatar, String usrName, AppInfo appInfo) {
        if (LogUtil.DEBUG) {
            LogUtil.log(TAG, "displayUsrInfo");
        }
        lock();

        mSpeakLayout.setVisibility(GONE);
        mListenLayout.setVisibility(GONE);
        mOptionsLayout.setVisibility(GONE);
        mUsrInfoLayout.setVisibility(VISIBLE);
        mUsrMsgLayout.setVisibility(GONE);
        mMsgSentLayout.setVisibility(GONE);

        Context context = getContext();
        Glide.with(context)
                .load(usrAvatar)
                .asBitmap()
                .error(R.drawable.kika_userpic_default)
                .transform(new FitCenter(context), new CropCircleTransformation(context))
                .diskCacheStrategy(DiskCacheStrategy.RESULT)
                .into(mUsrInfoAvatar);

        if (appInfo != null) {
            Glide.with(context)
                    .load(appInfo.getAppIcon())
                    .dontTransform()
                    .diskCacheStrategy(DiskCacheStrategy.RESULT)
                    .into(mUsrInfoImIcon);
        } else {
            Glide.clear(mUsrInfoImIcon);
        }

        mUsrInfoName.setText(usrName);

        onStatusChanged(ViewStatus.TTS);
    }

    public synchronized void displayUsrMsg(String usrAvatar, String usrName, String msgContent, AppInfo appInfo) {
        if (LogUtil.DEBUG) {
            LogUtil.log(TAG, "displayUsrMsg");
        }
        lock();

        mSpeakLayout.setVisibility(GONE);
        mListenLayout.setVisibility(GONE);
        mOptionsLayout.setVisibility(GONE);
        mUsrInfoLayout.setVisibility(GONE);
        mUsrMsgLayout.setVisibility(VISIBLE);

        Context context = getContext();
        Glide.with(context)
                .load(usrAvatar)
                .asBitmap()
                .error(R.drawable.kika_userpic_small_default)
                .transform(new FitCenter(context), new CropCircleTransformation(context))
                .diskCacheStrategy(DiskCacheStrategy.RESULT)
                .into(mUsrMsgAvatar);

        if (appInfo != null) {
            Glide.with(context)
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

    public synchronized void displayMsgSent() {
        if (LogUtil.DEBUG) {
            LogUtil.log(TAG, "displayMsgSent");
        }
        lock();

        mSpeakLayout.setVisibility(GONE);
        mListenLayout.setVisibility(GONE);
        mOptionsLayout.setVisibility(GONE);
        mUsrInfoLayout.setVisibility(GONE);
        mUsrMsgLayout.setVisibility(GONE);
        mMsgSentLayout.setVisibility(VISIBLE);

        onStatusChanged(ViewStatus.TTS);
    }


    public void onStatusChanged(final ViewStatus status) {
        onStatusChanged(status, null);
    }

    public void onStatusChanged(final ViewStatus status, IGifStatusListener listener) {
        ViewStatus nextStatus = getNextStatus(status);
        if (nextStatus == null) {
            return;
        }
        onNewStatus(nextStatus, listener);
    }

    private ViewStatus getNextStatus(ViewStatus status) {
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
                if (LogUtil.DEBUG) {
                    LogUtil.logv(TAG, String.format("current status: %1$s, target status: %2$s", mCurrentStatus.name(), status.name()));
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
                }
                break;
        }
        return nextStatus;
    }

    private void onNewStatus(ViewStatus status, final IGifStatusListener listener) {
        if (LogUtil.DEBUG) {
            LogUtil.logd(TAG, String.format("target status: %1$s", status.name()));
        }
        mCurrentStatus = status;
        switch (status) {
            case TTS:
                loadStatusGif(status, listener);
                break;
            case TTS_TO_LISTEN:
                loadStatusGif(status, new IGifStatusListener() {
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
                loadStatusGif(status, new GoLayout.IGifStatusListener() {
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
                loadStatusGif(status, new GoLayout.IGifStatusListener() {
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
                loadStatusGif(ViewStatus.ANALYZE, listener);
                break;
            case ANALYZE_TO_TTS:
                loadStatusGif(ViewStatus.ANALYZE_TO_TTS, new GoLayout.IGifStatusListener() {
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

    private synchronized void loadStatusGif(final ViewStatus status, final IGifStatusListener listener) {
        DialogFlowForegroundService.processStatusChanged(status);
        mUIHandler.removeCallbacksAndMessages(null);
        mUIHandler.post(new Runnable() {
            @Override
            public void run() {
                if (LogUtil.DEBUG) {
                    LogUtil.logv(TAG, String.format("status: %s", status.name()));
                }
                if (isTerminate) {
                    return;
                }
                Context context = getContext();

                if (listener != null) {
                    listener.onStart();
                }

                Glide.with(context)
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
                                        if (LogUtil.DEBUG) {
                                            LogUtil.logv(TAG, String.format("duration: %s", duration));
                                        }
                                        mUIHandler.postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                //发送延时消息，通知动画结束
                                                // handler.sendEmptyMessageDelayed(MESSAGE_SUCCESS,duration);
                                                listener.onStop(null);
                                            }
                                        }, duration);
                                    } else {
                                        if (LogUtil.DEBUG) {
                                            LogUtil.logv(TAG, "non-gif drawable");
                                        }
                                    }
                                }
                                return false;
                            }
                        }) //仅仅加载一次gif动画
                        .into(listener != null ? mNonRepeatTarget : mRepeatTarget);
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

    private boolean isTerminate;


    public void clear() {
        isTerminate = true;
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
