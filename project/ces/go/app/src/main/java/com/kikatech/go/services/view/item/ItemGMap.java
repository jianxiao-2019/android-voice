package com.kikatech.go.services.view.item;

import android.content.Context;
import android.graphics.PixelFormat;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.kikatech.go.R;
import com.kikatech.go.view.GoLayout;

/**
 * @author SkeeterWang Created on 2017/12/18.
 */

public class ItemGMap extends WindowFloatingItem {

    private ImageView mStatusView;
    private View mStatusWrapperView;
    private ImageView mMsgStatusView;

    public ItemGMap(View view, View.OnTouchListener listener) {
        super(view, listener);
    }

    @Override
    protected void bindView() {
        mStatusWrapperView = mItemView.findViewById(R.id.gmap_status_wrapper);
        mStatusView = (ImageView) mItemView.findViewById(R.id.gmap_status);
        mMsgStatusView = (ImageView) mItemView.findViewById(R.id.gmap_msg_status);
    }

    public void updateStatus(Context context, GoLayout.ViewStatus status) {
        if (mStatusWrapperView.getVisibility() == View.GONE) {
            mStatusWrapperView.setVisibility(View.VISIBLE);
        }

        Glide.with(context.getApplicationContext())
                .load(status.getSmallRes())
                .dontTransform()
                .dontAnimate()
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .into(mStatusView);
    }

    public void showMsgStatusView(Context context, boolean isSucceed) {
        if (mMsgStatusView.getVisibility() == View.GONE) {
            mMsgStatusView.setVisibility(View.VISIBLE);
        }

        Glide.with(context.getApplicationContext())
                .load(isSucceed ? R.drawable.kika_gmap_msgvui_success : R.drawable.kika_gmap_msgvui_alarm)
                .placeholder(isSucceed ? R.drawable.kika_gmap_msgvui_success : R.drawable.kika_gmap_msgvui_alarm)
                .dontTransform()
                .dontAnimate()
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .into(mMsgStatusView);
    }

    public void hideMsgStatusView() {
        mMsgStatusView.setVisibility(View.GONE);
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
