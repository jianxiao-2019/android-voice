package com.kikatech.go.services.view.item;

import android.databinding.ViewDataBinding;
import android.graphics.PixelFormat;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.kikatech.go.R;
import com.kikatech.go.databinding.GoLayoutGmapAsrResultBinding;

/**
 * @author SkeeterWang Created on 2018/1/25.
 */

public class ItemAsrResult extends WindowFloatingItem {
    private GoLayoutGmapAsrResultBinding mBinding;

    public ItemAsrResult(View view, View.OnTouchListener listener) {
        super(view, listener);
    }

    @Override
    protected <T extends ViewDataBinding> void onBindView(T binding) {
        mBinding = (GoLayoutGmapAsrResultBinding) binding;
    }

    public void setText(String text) {
        mBinding.gmapTvAsrResult.setText(text);
    }

    @Override
    public int getMeasuredWidth() {
        mBinding.gmapTvAsrResult.measure(0, 0);
        mItemView.measure(0, 0);
        return mItemView.getMeasuredWidth();
    }

    @Override
    public int getMeasuredHeight() {
        mBinding.gmapTvAsrResult.measure(0, 0);
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
                mBinding.gmapTvAsrResult.setBackgroundResource(R.drawable.kika_gmap_msg_1line);
                break;
            case Gravity.RIGHT:
                mBinding.gmapTvAsrResult.setBackgroundResource(R.drawable.kika_gmap_msg_1line_left);
                break;
        }
    }
}
