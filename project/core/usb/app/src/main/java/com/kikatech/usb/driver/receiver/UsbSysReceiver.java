package com.kikatech.usb.driver.receiver;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * @author SkeeterWang Created on 2018/6/22.
 */

public final class UsbSysReceiver extends BroadcastReceiver {
    private static final String TAG = "UsbSysReceiver";

    public static final String ACTION_USB_DEVICE_PERMISSION_GRANTED = "com.kikatech.usb.device.USB_PERMISSION";

    @SuppressLint("UnsafeProtectedBroadcastReceiver")
    @Override
    public void onReceive(Context context, Intent intent) {
        UsbSysIntentProcessor.processIntent(TAG, intent);
    }
}
