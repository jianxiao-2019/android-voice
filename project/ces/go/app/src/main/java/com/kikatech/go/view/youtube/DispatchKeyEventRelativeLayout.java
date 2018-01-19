package com.kikatech.go.view.youtube;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.widget.RelativeLayout;

/**
 * @author SkeeterWang Created on 2018/1/15.
 */

public class DispatchKeyEventRelativeLayout extends RelativeLayout {
    private static final String TAG = "FloatingPlayerView";

    private OnBackKeyListener mOnBackKeyListener;


    public DispatchKeyEventRelativeLayout(Context context) {
        super(context);
        init();
    }

    public DispatchKeyEventRelativeLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public DispatchKeyEventRelativeLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
    }


    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_UP && event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
            if (mOnBackKeyListener != null) {
                mOnBackKeyListener.onBackPressed();
            }
        }
        return super.dispatchKeyEvent(event);
    }


    public void setOnBackKeyListener(OnBackKeyListener listener) {
        mOnBackKeyListener = listener;
    }

    interface OnBackKeyListener {
        void onBackPressed();
    }
}
