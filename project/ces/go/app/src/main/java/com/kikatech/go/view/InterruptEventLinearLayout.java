package com.kikatech.go.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.LinearLayout;

/**
 * @author SkeeterWang Created on 2017/12/21.
 */

public class InterruptEventLinearLayout extends LinearLayout {
    private static final String TAG = "InterruptEventLinearLayout";

    public InterruptEventLinearLayout(Context context) {
        super(context);
        init();
    }

    public InterruptEventLinearLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public InterruptEventLinearLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return true;
    }
}
