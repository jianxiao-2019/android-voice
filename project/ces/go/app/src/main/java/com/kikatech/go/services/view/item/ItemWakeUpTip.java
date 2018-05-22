package com.kikatech.go.services.view.item;

import android.graphics.PixelFormat;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.kikatech.go.R;

/**
 * @author SkeeterWang Created on 2017/12/18.
 */

public class ItemWakeUpTip extends WindowFloatingItem {
    private TextView mMsgViewText;

    public ItemWakeUpTip(View view, View.OnTouchListener listener) {
        super(view, listener);
    }

    @Override
    protected void bindView() {
        mMsgViewText = (TextView) mItemView.findViewById(R.id.gmap_msg);
    }

    public void setText(String text) {
        mMsgViewText.setText(text);
    }

    @Override
    public int getMeasuredWidth() {
        mMsgViewText.measure(0, 0);
        mItemView.measure(0, 0);
        return mItemView.getMeasuredWidth();
    }

    @Override
    public int getMeasuredHeight() {
        mMsgViewText.measure(0, 0);
        mItemView.measure(0, 0);
        return mItemView.getMeasuredHeight();
    }

    @Override
    protected WindowManager.LayoutParams getDefaultParam() {
        return new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_PRIORITY_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                PixelFormat.TRANSLUCENT
        );
    }

    public void updateBackgroundRes(int gravity) {
        switch (gravity) {
            case Gravity.LEFT:
                mMsgViewText.setBackgroundResource(R.drawable.kika_gmap_msg_1line);
                break;
            case Gravity.RIGHT:
                mMsgViewText.setBackgroundResource(R.drawable.kika_gmap_msg_1line_left);
                break;
        }
    }
}
