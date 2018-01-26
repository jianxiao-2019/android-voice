package com.kikatech.go.services.view.item;

import android.graphics.PixelFormat;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.kikatech.go.R;

/**
 * @author SkeeterWang Created on 2018/1/25.
 */

public class ItemAsrResult extends WindowFloatingItem {
    private TextView mTvAsrResult;

    public ItemAsrResult(View view, View.OnTouchListener listener) {
        super(view, listener);
    }

    @Override
    protected void bindView() {
        mTvAsrResult = (TextView) mItemView.findViewById(R.id.gmap_tv_asr_result);
    }

    public void setText(String text) {
        mTvAsrResult.setText(text);
    }

    @Override
    public int getMeasuredWidth() {
        mTvAsrResult.measure(0, 0);
        mItemView.measure(0, 0);
        return mItemView.getMeasuredWidth();
    }

    @Override
    public int getMeasuredHeight() {
        mTvAsrResult.measure(0, 0);
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
                mTvAsrResult.setBackgroundResource(R.drawable.kika_gmap_msg_1line);
                break;
            case Gravity.RIGHT:
                mTvAsrResult.setBackgroundResource(R.drawable.kika_gmap_msg_1line_left);
                break;
        }
    }
}
