package com.kikatech.go.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.kikatech.go.R;
import com.kikatech.go.view.widget.GoTextView;
import com.kikatech.voice.core.dialogflow.scene.SceneBase;

import java.util.List;

/**
 * @author SkeeterWang Created on 2017/11/10.
 */
public class GoLayout extends RelativeLayout {
    private static final String TAG = "GoLayout";

    private enum DisplayMode {
        SLEEP, AWAKE
    }

    private enum ViewStatus {
        SPEAK, LISTEN, DISPLAY_OPTIONS
    }

    private static final int DEFAULT_SPEAK_TEXT_SIZE_SP = 40;
    private static final int DEFAULT_SPEAK_TEXT_COLOR = Color.GRAY;
    private static final int DEFAULT_LISTEN_TEXT_SIZE_SP = 40;
    private static final int DEFAULT_LISTEN_TEXT_COLOR = Color.BLACK;
    private static final int DEFAULT_OPTION_TITLE_TEXT_SIZE_SP = 40;
    private static final int DEFAULT_OPTION_TITLE_TEXT_COLOR = Color.BLACK;
    private static final int DEFAULT_OPTION_ITEM_TEXT_SIZE_SP = 30;
    private static final int DEFAULT_OPTION_ITEM_TEXT_COLOR = Color.GRAY;
    private static final int MIN_TEXT_SIZE_SP = 20;

    private DisplayMode mCurrentMode = DisplayMode.AWAKE;
    private ViewStatus mCurrentStatus;

    private float mSpeakTextSize;
    private int mSpeakTextColor;
    private float mListenTextSize;
    private int mListenTextColor;
    private float mOptionTitleTextSize;
    private int mOptionTitleTextColor;
    private float mOptionItemTextSize;
    private int mOptionItemTextColor;

    private GoTextView mSpeakView;
    private GoTextView mListenView;
    private GoTextView mOptionsTitle;
    private LinearLayout mOptionsLayout;
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
        init(attrs);
    }

    private void init(AttributeSet attrs) {
        try {
            Context context = getContext();

            if (attrs != null) {
                TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.GoLayout, 0, 0);

                mSpeakTextSize = typedArray.getFloat(R.styleable.GoLayout_speak_text_size, DEFAULT_SPEAK_TEXT_SIZE_SP);
                mSpeakTextColor = typedArray.getColor(R.styleable.GoLayout_speak_text_color, DEFAULT_SPEAK_TEXT_COLOR);

                mListenTextSize = typedArray.getFloat(R.styleable.GoLayout_listen_text_size, DEFAULT_LISTEN_TEXT_SIZE_SP);
                mListenTextColor = typedArray.getColor(R.styleable.GoLayout_listen_text_color, DEFAULT_LISTEN_TEXT_COLOR);

                mOptionTitleTextSize = typedArray.getFloat(R.styleable.GoLayout_listen_text_size, DEFAULT_OPTION_TITLE_TEXT_SIZE_SP);
                mOptionTitleTextColor = typedArray.getColor(R.styleable.GoLayout_listen_text_color, DEFAULT_OPTION_TITLE_TEXT_COLOR);
                mOptionItemTextSize = typedArray.getFloat(R.styleable.GoLayout_listen_text_size, DEFAULT_OPTION_ITEM_TEXT_SIZE_SP);
                mOptionItemTextColor = typedArray.getColor(R.styleable.GoLayout_listen_text_color, DEFAULT_OPTION_ITEM_TEXT_COLOR);

                typedArray.recycle();
            }

            initSpeakView(context);
            initListenView(context);
            initOptionView(context);
            initStatusView(context);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initSpeakView(Context context) {
        mSpeakView = new GoTextView(context);
        LayoutParams speakParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        speakParams.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
        mSpeakView.setLayoutParams(speakParams);
        mSpeakView.setTextColor(mSpeakTextColor);
        mSpeakView.enableResize(sp2px(MIN_TEXT_SIZE_SP), sp2px(mSpeakTextSize));
        mSpeakView.setGravity(Gravity.CENTER);
        addView(mSpeakView);
    }

    private void initListenView(Context context) {
        mListenView = new GoTextView(context);
        LayoutParams listenParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        listenParams.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
        mListenView.setLayoutParams(listenParams);
        mListenView.setTextColor(mListenTextColor);
        mListenView.setGravity(Gravity.CENTER);
        mListenView.enableResize(sp2px(MIN_TEXT_SIZE_SP), sp2px(mListenTextSize));
        addView(mListenView);
    }

    private void initOptionView(Context context) {
        mOptionsTitle = new GoTextView(context);
        mOptionsLayout = new LinearLayout(context);
        mOptionsLayout.setOrientation(LinearLayout.VERTICAL);

        LayoutParams titleParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        titleParams.addRule(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.TRUE);
        mOptionsTitle.setLayoutParams(titleParams);
        mOptionsTitle.setTextColor(mOptionTitleTextColor);
        mOptionsTitle.enableResize(sp2px(MIN_TEXT_SIZE_SP), sp2px(mOptionTitleTextSize));
        mOptionsTitle.setGravity(Gravity.CENTER);

        LayoutParams optionParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        optionParams.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
        mOptionsLayout.setLayoutParams(optionParams);

        addView(mOptionsTitle);
        addView(mOptionsLayout);
    }

    private void initStatusView(Context context) {
        mStatusAnimationView = new TextView(context);
        LayoutParams statusParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        statusParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
        statusParams.addRule(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.TRUE);
        mStatusAnimationView.setTextColor(getResources().getColor(android.R.color.darker_gray));
        mStatusAnimationView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 26);
        mStatusAnimationView.setLayoutParams(statusParams);
        addView(mStatusAnimationView);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        switch (mCurrentMode) {
            case SLEEP:
                setBackgroundColor(getResources().getColor(android.R.color.black));
                break;
            case AWAKE:
                setBackgroundColor(getResources().getColor(android.R.color.white));
                break;
        }
    }


    public void onModeChanged(DisplayMode mode) {
        mCurrentMode = (mCurrentMode.equals(DisplayMode.SLEEP)) ? DisplayMode.AWAKE : DisplayMode.SLEEP;
        requestLayout();
    }

    public void speak(String text) {
        mCurrentStatus = ViewStatus.SPEAK;

        mSpeakView.setVisibility(VISIBLE);
        mListenView.setVisibility(GONE);
        mOptionsTitle.setVisibility(GONE);
        mOptionsLayout.setVisibility(GONE);
        mStatusAnimationView.setText("Speaking"); // TODO: animation
        mSpeakView.setText(text);
    }

    public void listen(String text) {
        mCurrentStatus = ViewStatus.LISTEN;

        mSpeakView.setVisibility(GONE);
        mListenView.setVisibility(VISIBLE);
        mOptionsTitle.setVisibility(GONE);
        mOptionsLayout.setVisibility(GONE);
        mStatusAnimationView.setText("Listening"); // TODO: animation
        mListenView.setText(text);
    }

    public void displayOptions(String title, final SceneBase.OptionList optionList, final IOnOptionSelectListener listener) {
        mCurrentStatus = ViewStatus.DISPLAY_OPTIONS;

        mSpeakView.setVisibility(GONE);
        mListenView.setVisibility(GONE);
        mOptionsTitle.setVisibility(VISIBLE);
        mOptionsLayout.setVisibility(VISIBLE);

        mOptionsTitle.setText(title);
        mOptionsLayout.removeAllViews();
        Context context = getContext();
        for (final SceneBase.Option option : optionList.getList()) {
            TextView optionView = new TextView(context);
            LinearLayout.LayoutParams optionParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            optionParams.setMargins(0, dp2px(20), 0, 0);
            optionView.setLayoutParams(optionParams);
            optionView.setTextColor(mOptionItemTextColor);
            optionView.setTextSize(TypedValue.COMPLEX_UNIT_SP, mOptionItemTextSize);
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
            mOptionsLayout.addView(optionView);
        }

        mStatusAnimationView.setText("Display Options"); // TODO: animation
    }


    private int sp2px(float sp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, getContext().getResources().getDisplayMetrics());
    }

    private int dp2px(float dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getContext().getResources().getDisplayMetrics());
    }


    public interface IOnOptionSelectListener {
        void onSelected(byte requestType, SceneBase.Option option);
    }
}
