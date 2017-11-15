package com.kikatech.go.view.widget;

import android.content.Context;
import android.support.v7.widget.AppCompatTextView;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.TypedValue;

import com.kikatech.go.util.LogUtil;


/**
 * @author SkeeterWang Created on 2017/11/10.
 */
public class GoTextView extends AppCompatTextView {

    private static final String TAG = "GoTextView";

    private float mMinTextSize;
    private float mMaxTextSize;
    private boolean mResizeEnabled;
    private boolean mNeedResize;
    private float mTextSize;


    public GoTextView(Context context) {
        super(context);
    }

    public GoTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public GoTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


    /**
     * Resize text after measuring
     */
    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        if (mResizeEnabled && (changed || mNeedResize)) {
            int widthLimit = (right - left) - getCompoundPaddingLeft() - getCompoundPaddingRight();
            int heightLimit = (bottom - top) - getCompoundPaddingBottom() - getCompoundPaddingTop();
            resizeText(widthLimit, heightLimit);
        }
        super.onLayout(changed, left, top, right, bottom);
    }

    /**
     * When text changes, set the force resize flag to true and reset the text size.
     */
    @Override
    protected void onTextChanged(final CharSequence text, final int start, final int before, final int after) {
        if (mResizeEnabled) {
            mNeedResize = true;
            // Since this view may be reused, it is good to reset the text size
            resetTextSize();
        }
    }

    /**
     * If the text view size changed, set the force resize flag to true
     */
    @Override
    protected void onSizeChanged(int width, int height, int oldWidth, int oldHeight) {
        if (mResizeEnabled) {
            if (width != oldWidth || height != oldHeight) {
                mNeedResize = true;
            }
        }
    }

    /**
     * Override the set text size to update our internal reference values
     */
    @Override
    public void setTextSize(float size) {
        super.setTextSize(size);
        mTextSize = getTextSize();
    }

    /**
     * Override the set text size to update our internal reference values
     */
    @Override
    public void setTextSize(int unit, float size) {
        super.setTextSize(unit, size);
        mTextSize = getTextSize();
    }


    /**
     * Enable resize feature with specified size range, and invalidate the view
     *
     * @param minTextSize lower text size limit, unit px
     * @param maxTextSize upper text size limit, unit px
     **/
    public void enableResize(float minTextSize, float maxTextSize) {
        mResizeEnabled = true;
        mMinTextSize = minTextSize;
        mMaxTextSize = maxTextSize;
        setTextSize(TypedValue.COMPLEX_UNIT_PX, maxTextSize);
        requestLayout();
        invalidate();
    }

    /**
     * Reset the text to the original size
     */
    public void resetTextSize() {
        if (mTextSize > 0) {
            super.setTextSize(TypedValue.COMPLEX_UNIT_PX, mMaxTextSize);
            mTextSize = mMaxTextSize;
        }
    }

    /**
     * Resize the text size with specified width and height
     */
    public void resizeText(int width, int height) {
        CharSequence text = getText();
        // Do not resize if the view does not have dimensions or there is no text
        if (text == null || text.length() == 0 || height <= 0 || width <= 0 || mTextSize == 0) {
            return;
        }

        if (getTransformationMethod() != null) {
            text = getTransformationMethod().getTransformation(text, this);
        }

        // Get the text view's paint object
        TextPaint textPaint = getPaint();

        // If resize enabled, use the lesser of that and the default text size
        float targetTextSize = mResizeEnabled ? Math.min(mTextSize, mMaxTextSize) : mTextSize;

        StaticLayout staticLayout = getStaticLayout(text, textPaint, width, targetTextSize);
        int textHeight = staticLayout.getHeight();

        while ((textHeight > height || staticLayout.getLineCount() > 1) && targetTextSize > mMinTextSize) {
            targetTextSize = Math.max(targetTextSize - 2, mMinTextSize);
            staticLayout = getStaticLayout(text, textPaint, width, targetTextSize);
            textHeight = staticLayout.getHeight();
        }

        // Some devices try to auto adjust line spacing, so force default line spacing
        // and invalidate the layout as a side effect
        setTextSize(TypedValue.COMPLEX_UNIT_PX, targetTextSize);

        // Reset force resize flag
        mNeedResize = false;
    }

    // Set the text size of the text paint object and use a static layout to render text off screen before measuring
    private StaticLayout getStaticLayout(CharSequence source, TextPaint paint, int width, float textSize) {
        // modified: make a copy of the original TextPaint object for measuring
        // (apparently the object gets modified while measuring, see also the
        // docs for TextView.getPaint() (which states to access it read-only)
        TextPaint paintCopy = new TextPaint(paint);
        // Update the text paint object
        paintCopy.setTextSize(textSize);
        // Measure using a static layout
        StaticLayout layout = new StaticLayout(source, paintCopy, width, Layout.Alignment.ALIGN_NORMAL, 1.0f, 0.0f, true);
        return layout;
    }
}