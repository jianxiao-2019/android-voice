package com.kikatech.go.services.view.item;

import android.graphics.PixelFormat;
import android.view.View;
import android.view.WindowManager;

/**
 * @author SkeeterWang Created on 2018/2/9.
 */

public class ItemOobe extends WindowFloatingItem {

    public ItemOobe(View view, View.OnTouchListener listener) {
        super(view, listener);
    }

    @Override
    protected void bindView() {
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