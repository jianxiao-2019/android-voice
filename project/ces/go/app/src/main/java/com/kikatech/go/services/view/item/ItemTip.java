package com.kikatech.go.services.view.item;

import android.databinding.ViewDataBinding;
import android.graphics.PixelFormat;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;

import com.kikatech.go.R;
import com.kikatech.go.databinding.GoLayoutGmapTipBinding;

/**
 * @author SkeeterWang Created on 2017/12/18.
 */

public class ItemTip extends WindowFloatingItem {

    private GoLayoutGmapTipBinding mBinding;

    public ItemTip(View view, View.OnTouchListener listener) {
        super(view, listener);
    }

    @Override
    protected <T extends ViewDataBinding> void onBindView(T binding) {
        mBinding = (GoLayoutGmapTipBinding) binding;
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

    public void setTitle(String title) {
        mBinding.itemTipTitle.setText(title);
    }

    public void setText(String text) {
        mBinding.itemTipText.setText(text);
    }

    public void updateBackgroundRes(int gravity) {
        switch (gravity) {
            case Gravity.LEFT:
                mItemView.setBackgroundResource(R.drawable.kika_gmap_msg_2line_left);
                break;
            case Gravity.RIGHT:
                mItemView.setBackgroundResource(R.drawable.kika_gmap_msg_2line);
                break;
        }
    }
}
