package com.kikatech.go.view.youtube;

import android.content.Context;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.FrameLayout;

/**
 * @author SkeeterWang Created on 2017/6/20.
 */

public class VideoTouchRoot extends FrameLayout {
    public static final int MIN_INTERCEPTION_TIME = 1000;
    private long lastInterception;

    private OnTouchReceiver touchReceiver;

    public VideoTouchRoot(final Context context) {
        super(context);
    }

    public VideoTouchRoot(final Context context, final AttributeSet attrs) {
        super(context, attrs);
    }

    public VideoTouchRoot(final Context context, final AttributeSet attrs, final int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public boolean dispatchTouchEvent(final MotionEvent ev) {
        if (touchReceiver != null) {
            final long timeStamp = SystemClock.elapsedRealtime();
            // we throttle the touch event dispatch to avoid event spam
            if (timeStamp - lastInterception > MIN_INTERCEPTION_TIME) {
                lastInterception = timeStamp;
                touchReceiver.onControllerUiTouched();
            }
        }
        return super.dispatchTouchEvent(ev);
    }

    public void setOnTouchReceiver(final OnTouchReceiver receiver) {
        this.touchReceiver = receiver;
    }

    public interface OnTouchReceiver {
        void onControllerUiTouched();
    }
}