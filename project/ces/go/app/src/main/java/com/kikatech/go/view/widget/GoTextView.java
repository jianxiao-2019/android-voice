package com.kikatech.go.view.widget;

import android.content.Context;
import android.support.v7.widget.AppCompatTextView;
import android.util.AttributeSet;

import com.kikatech.go.view.ResizeHelper;


/**
 * @author SkeeterWang Created on 2017/11/10.
 */
public class GoTextView extends AppCompatTextView {

    private static final String TAG = "GoTextView";

    private ResizeHelper mHelper;

    public GoTextView(Context context) {
        this(context, null);
    }

    public GoTextView(Context context, AttributeSet attrs) {
        this(context, attrs, android.R.attr.textViewStyle);
    }

    public GoTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    private void init(AttributeSet attrs) {
        mHelper = ResizeHelper.create(this, attrs);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        if (mHelper != null) {
            mHelper.prepareResize();
        }
        super.onLayout(changed, left, top, right, bottom);
    }

    public void enableResize() {
        if (mHelper != null) {
            mHelper.enableResize();
        }
    }

    public void disableResize(float targetSize) {
        if (mHelper != null) {
            mHelper.disableResize(targetSize);
        }
    }

    public float getMaxTextSize() {
        if (mHelper != null) {
            return mHelper.getMaxTextSize();
        }
        return getTextSize();
    }

    public float getMinTextSize() {
        if (mHelper != null) {
            return mHelper.getMinTextSize();
        }
        return getTextSize();
    }
}