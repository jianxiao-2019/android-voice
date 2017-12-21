package com.kikatech.go.services.view.item;

import android.graphics.PixelFormat;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.kikatech.go.R;
import com.kikatech.go.services.DialogFlowForegroundService;
import com.kikatech.go.util.LogUtil;

/**
 * @author SkeeterWang Created on 2017/12/18.
 */

public class BtnClose extends WindowFloatingButton {
    private static final String TAG = "BtnClose";

    public BtnClose(View view, View.OnTouchListener listener) {
        super(view, listener);
    }

    @Override
    protected void bindView() {
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
        TextView mBtnText = (TextView) mItemView.findViewById(R.id.btn_text);
        if (mBtnText != null) {
            mBtnText.setTextColor(mBtnText.getContext().getResources().getColor(R.color.floating_btn_close));
        }
        ImageView mBtnIcon = (ImageView) mItemView.findViewById(R.id.btn_icon);
        if (mBtnIcon != null) {
            Glide.with(mBtnIcon.getContext().getApplicationContext())
                    .load(R.drawable.kika_floating_close_pressed)
                    .dontTransform()
                    .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                    .into(mBtnIcon);
        }
    }

    @Override
    public void onLeaved() {
        if (LogUtil.DEBUG) {
            LogUtil.log(TAG, "onLeaved");
        }
        TextView mBtnText = (TextView) mItemView.findViewById(R.id.btn_text);
        if (mBtnText != null) {
            mBtnText.setTextColor(mBtnText.getContext().getResources().getColor(android.R.color.white));
        }
        ImageView mBtnIcon = (ImageView) mItemView.findViewById(R.id.btn_icon);
        if (mBtnIcon != null) {
            Glide.with(mBtnIcon.getContext().getApplicationContext())
                    .load(R.drawable.kika_floating_close)
                    .dontTransform()
                    .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                    .into(mBtnIcon);
        }
    }

    @Override
    public void onSelected() {
        DialogFlowForegroundService.processStop(mItemView.getContext(), DialogFlowForegroundService.class);
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
