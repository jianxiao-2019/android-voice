package com.kikatech.go.services.view.item;

import android.databinding.ViewDataBinding;
import android.graphics.PixelFormat;
import android.view.View;
import android.view.WindowManager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.kikatech.go.R;
import com.kikatech.go.databinding.GoLayoutGmapBtnOpenAppBinding;
import com.kikatech.go.util.IntentUtil;
import com.kikatech.go.util.LogUtil;

/**
 * @author SkeeterWang Created on 2017/12/18.
 */

public class BtnOpenApp extends WindowFloatingButton {
    private static final String TAG = "BtnOpenApp";

    private GoLayoutGmapBtnOpenAppBinding mBinding;

    public BtnOpenApp(View view, View.OnTouchListener listener) {
        super(view, listener);
    }

    @Override
    protected <T extends ViewDataBinding> void onBindView(T binding) {
        mBinding = (GoLayoutGmapBtnOpenAppBinding) binding;
    }

    @Override
    public void show() {
        animatePopUp();
    }

    @Override
    public void hide() {
        animatePopDown();
    }

    @Override
    public void onEnter() {
        if (LogUtil.DEBUG) {
            LogUtil.log(TAG, "onEnter");
        }
        mBinding.btnText.setTextColor(mBinding.btnText.getContext().getResources().getColor(R.color.floating_btn_open));
        Glide.with(mBinding.btnIcon.getContext().getApplicationContext())
                .load(R.drawable.kika_floating_open_pressed)
                .dontTransform()
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .into(mBinding.btnIcon);
    }

    @Override
    public void onLeaved() {
        if (LogUtil.DEBUG) {
            LogUtil.log(TAG, "onLeaved");
        }
        mBinding.btnText.setTextColor(mBinding.btnText.getContext().getResources().getColor(android.R.color.white));
        Glide.with(mBinding.btnIcon.getContext().getApplicationContext())
                .load(R.drawable.kika_floating_open)
                .dontTransform()
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .into(mBinding.btnIcon);
    }

    @Override
    public void onSelected() {
        IntentUtil.openKikaGo(mItemView.getContext());
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
}
