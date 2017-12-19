package com.kikatech.go.services.view.item;

import android.graphics.PixelFormat;
import android.view.View;
import android.view.WindowManager;

/**
 * @author SkeeterWang Created on 2017/12/18.
 */

public class BtnClose extends WindowFloatingButton {

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
