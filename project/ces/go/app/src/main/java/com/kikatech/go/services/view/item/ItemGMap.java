package com.kikatech.go.services.view.item;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.graphics.PixelFormat;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.kikatech.go.R;
import com.kikatech.go.databinding.GoLayoutGmapBinding;
import com.kikatech.go.view.GoLayout;

/**
 * @author SkeeterWang Created on 2017/12/18.
 */

public class ItemGMap extends WindowFloatingItem {

    private GoLayoutGmapBinding mBinding;

    public ItemGMap(View view, View.OnTouchListener listener) {
        super(view, listener);
    }

    @Override
    protected <T extends ViewDataBinding> void onBindView(T binding) {
        mBinding = (GoLayoutGmapBinding) binding;
    }

    public void updateStatus(Context context, GoLayout.ViewStatus status) {
        if (mBinding.gmapStatusWrapper.getVisibility() == View.GONE) {
            mBinding.gmapStatusWrapper.setVisibility(View.VISIBLE);
        }

        Glide.with(context.getApplicationContext())
                .load(status.getSmallRes())
                .dontTransform()
                .dontAnimate()
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .into(mBinding.gmapStatus);
    }

    public void showMsgStatusView(Context context, boolean isSucceed) {
        if (mBinding.gmapMsgStatus.getVisibility() == View.GONE) {
            mBinding.gmapMsgStatus.setVisibility(View.VISIBLE);
        }

        Glide.with(context.getApplicationContext())
                .load(isSucceed ? R.drawable.kika_gmap_msgvui_success : R.drawable.kika_gmap_msgvui_alarm)
                .placeholder(isSucceed ? R.drawable.kika_gmap_msgvui_success : R.drawable.kika_gmap_msgvui_alarm)
                .dontTransform()
                .dontAnimate()
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .into(mBinding.gmapMsgStatus);
    }

    public void hideMsgStatusView() {
        mBinding.gmapMsgStatus.setVisibility(View.GONE);
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
