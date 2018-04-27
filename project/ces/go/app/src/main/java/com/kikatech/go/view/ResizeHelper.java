package com.kikatech.go.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.text.Editable;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.TextWatcher;
import android.text.method.TransformationMethod;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.widget.TextView;

import com.kikatech.go.R;
import com.kikatech.go.util.ResolutionUtil;
import com.kikatech.go.util.LogUtil;

/**
 * @author SkeeterWang Created on 2017/11/18.
 */
public class ResizeHelper {
    private static final String TAG = "ResizeHelper";

    private static final int DEFAULT_MIN_TEXT_SIZE_SP = 30;

    public static ResizeHelper create(TextView view, AttributeSet attrs) {
        return new ResizeHelper(view, attrs);
    }


    private TextView mTextView;
    private TextPaint mPaint;

    private float mMinTextSize;
    private float mMaxTextSize;

    private boolean mResizeEnabled;

    private ResizeHelper(TextView view, AttributeSet attrs) {
        mTextView = view;
        mPaint = new TextPaint();
        try {
            Context context = view.getContext();

            if (attrs != null) {
                TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.GoTextView, 0, 0);

                mMinTextSize = typedArray.getDimensionPixelSize(R.styleable.GoTextView_min_text_size, ResolutionUtil.sp2px(context, DEFAULT_MIN_TEXT_SIZE_SP));
                mMaxTextSize = typedArray.getDimensionPixelSize(R.styleable.GoTextView_max_text_size, -1);

                if (mMaxTextSize > 0) {
                    mResizeEnabled = true;
                    view.setTextSize(TypedValue.COMPLEX_UNIT_PX, mMaxTextSize);
                }

                typedArray.recycle();
            }
        } catch (Exception e) {
            if (LogUtil.DEBUG) {
                LogUtil.printStackTrace(TAG, e.getMessage(), e);
            }
        }

        mTextView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                resize();
            }
        });

        mTextView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                resize();
            }
        });
    }

    /**
     * hide view for resizing
     **/
    public void prepareResize() {
        if (mResizeEnabled) {
            mTextView.setVisibility(View.INVISIBLE);
        }
    }

    private void resize() {
        if (!mResizeEnabled) {
            return;
        }

        int targetWidth = mTextView.getWidth() - mTextView.getPaddingLeft() - mTextView.getPaddingRight();
        if (targetWidth <= 0) {
            return;
        }

        CharSequence text = mTextView.getText();
        TransformationMethod method = mTextView.getTransformationMethod();
        if (method != null) {
            text = method.getTransformation(text, mTextView);
        }

        float targetTextSize = mMaxTextSize;

        mPaint.set(mTextView.getPaint());
        mPaint.setTextSize(targetTextSize);

        StaticLayout staticLayout = getStaticLayout(text, mPaint, targetWidth, targetTextSize);

        Context context = mTextView.getContext();
        while (staticLayout.getLineCount() > 1 && targetTextSize > mMinTextSize) {
            targetTextSize = Math.max(targetTextSize - ResolutionUtil.dp2px(context, 1), mMinTextSize);
            staticLayout = getStaticLayout(text, mPaint, targetWidth, targetTextSize);
        }

        final float finalTargetTextSize = targetTextSize;
        mTextView.post(new Runnable() {
            @Override
            public void run() {
                mTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, finalTargetTextSize);
                mTextView.setVisibility(View.VISIBLE);
            }
        });
    }

    public void enableResize() {
        mResizeEnabled = true;
    }

    public void disableResize(float targetSize) {
        mResizeEnabled = false;
        mTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, targetSize);
    }

    public float getMaxTextSize() {
        return mMaxTextSize;
    }

    public float getMinTextSize() {
        return mMinTextSize;
    }

    // Set the text size of the text paint object and use a static layout to render text off screen before measuring
    private StaticLayout getStaticLayout(CharSequence source, TextPaint paint, int width, float textSize) {
        TextPaint paintCopy = new TextPaint(paint);
        paintCopy.setTextSize(textSize);
        return new StaticLayout(source, paintCopy, width, Layout.Alignment.ALIGN_NORMAL, 1.0f, 0.0f, true);
    }
}
