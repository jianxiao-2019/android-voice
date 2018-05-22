package com.kikatech.go.services.view.item;

import android.databinding.ViewDataBinding;
import android.graphics.PixelFormat;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;

import com.kikatech.go.R;
import com.kikatech.go.databinding.GoLayoutGmapMsgBinding;

/**
 * @author SkeeterWang Created on 2017/12/18.
 */

public class ItemMsg extends WindowFloatingItem {

    private GoLayoutGmapMsgBinding mBinding;

    public ItemMsg(View view, View.OnTouchListener listener) {
        super(view, listener);
    }

    @Override
    protected <T extends ViewDataBinding> void onBindView(T binding) {
        mBinding = (GoLayoutGmapMsgBinding) binding;
    }

    public void setText(String text) {
        mBinding.gmapMsg.setText(text);
    }

    @Override
    public int getMeasuredWidth() {

        mBinding.gmapMsg.measure(0, 0);
        mItemView.measure(0, 0);
        return mItemView.getMeasuredWidth();
    }

    @Override
    public int getMeasuredHeight() {
        mBinding.gmapMsg.measure(0, 0);
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
                mBinding.gmapMsg.setBackgroundResource(R.drawable.kika_gmap_msg_1line);
                break;
            case Gravity.RIGHT:
                mBinding.gmapMsg.setBackgroundResource(R.drawable.kika_gmap_msg_1line_left);
                break;
        }
    }
}
