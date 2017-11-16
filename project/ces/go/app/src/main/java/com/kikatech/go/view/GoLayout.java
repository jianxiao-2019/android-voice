package com.kikatech.go.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.kikatech.go.R;
import com.kikatech.go.dialogflow.model.Option;
import com.kikatech.go.dialogflow.model.OptionList;
import com.kikatech.go.util.LogUtil;
import com.kikatech.go.view.widget.GoTextView;

/**
 * @author SkeeterWang Created on 2017/11/10.
 */
public class GoLayout extends FrameLayout {
    private static final String TAG = "GoLayout";

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


    public void onModeChanged(DisplayMode mode) {
        mCurrentMode = (mCurrentMode.equals(DisplayMode.SLEEP)) ? DisplayMode.AWAKE : DisplayMode.SLEEP;
        requestLayout();
    }


    public synchronized void speak(final String text) {
        mCurrentStatus = ViewStatus.SPEAK;

        mSpeakLayout.setVisibility(VISIBLE);
        mListenLayout.setVisibility(GONE);
        mOptionsLayout.setVisibility(GONE);

        mSpeakView.setText(text);

        onStatusChanged(mCurrentStatus);
    }

    public synchronized void listen(final String text) {
        mCurrentStatus = ViewStatus.LISTEN;

        mSpeakLayout.setVisibility(GONE);
        mListenLayout.setVisibility(VISIBLE);
        mOptionsLayout.setVisibility(GONE);

        mListenView.setText(text);

        onStatusChanged(mCurrentStatus);
    }

    public synchronized void displayOptions(final String title, final OptionList optionList, final IOnOptionSelectListener listener) {
        mCurrentStatus = ViewStatus.DISPLAY_OPTIONS;

        mSpeakLayout.setVisibility(GONE);
        mListenLayout.setVisibility(GONE);
        mOptionsLayout.setVisibility(VISIBLE);

        mOptionsLayout.removeAllViews();

        try {
            if (optionList != null && !optionList.isEmpty()) {
                mOptionsTitle.setText(title);
                mOptionsLayout.addView(mOptionsTitle);
                for (final Option option : optionList.getList()) {
                    GoTextView optionView = (GoTextView) mLayoutInflater.inflate(R.layout.go_layout_option_item, null);
                    mOptionsLayout.addView(optionView);
                    optionView.setText(option.getDisplayText());
                    optionView.setGravity(Gravity.CENTER);
                    optionView.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (listener != null) {
                                listener.onSelected(optionList.getRequestType(), option);
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
            final int CHILD_COUNT = mOptionsLayout.getChildCount();
            final int DEFAULT_TITLE_MARGIN_BOTTOM = 20; //px
            final int DEFAULT_TOTAL_MARGIN = 180; // px
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
            mOptionsLayout.requestLayout();

        } catch (Exception e) {
            if (LogUtil.DEBUG) {
                LogUtil.printStackTrace(TAG, e.getMessage(), e);
            }
        }
    }

    private void onStatusChanged(ViewStatus status) {
        switch (status) {
            case LOADING:
                mStatusAnimationView.setText("Loading"); // TODO: animation
                break;
            case SPEAK:
                mStatusAnimationView.setText("Speaking"); // TODO: animation
                break;
            case LISTEN:
                mStatusAnimationView.setText("Listening"); // TODO: animation
                break;
            case DISPLAY_OPTIONS:
                mStatusAnimationView.setText("Display Options"); // TODO: animation
                break;
        }
    }


    public interface IOnOptionSelectListener {
        void onSelected(byte requestType, Option option);
    }
}
