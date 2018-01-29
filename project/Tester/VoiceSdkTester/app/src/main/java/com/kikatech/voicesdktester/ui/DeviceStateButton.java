package com.kikatech.voicesdktester.ui;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Created by ryanlin on 23/01/2018.
 */

public class DeviceStateButton extends LinearLayout {

    private ImageView mSignalImage;
    private ImageView mIconImage;
    private TextView mText;

    public DeviceStateButton(Context context) {
        super(context);
        initView();
    }

    public DeviceStateButton(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public DeviceStateButton(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    private void initView() {
        mSignalImage = new ImageView(getContext());
        mIconImage = new ImageView(getContext());
        mText = new TextView(getContext());

        addView(mSignalImage);
        addView(mIconImage);
        addView(mText);
    }

    public void setSignal(int signal) {

    }

    public void setIcon(int resId) {
        mIconImage.setImageResource(resId);
    }

    public void setText(int resId) {
        mText.setText(resId);
    }

    public void setText(String text) {
        mText.setText(text);
    }
}
