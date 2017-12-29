package com.kikatech.go.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbManager;

import com.kikatech.go.util.LogUtil;

/**
 * Created by brad_chang on 2017/12/21.
 */

public class UsbDeviceStateReceiver extends BroadcastReceiver {
    static String TAG = "UsbDeviceStateReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (LogUtil.DEBUG) {
            LogUtil.logv(TAG, "onReceive:" + intent.getAction());
        }
        String action = intent != null ? intent.getAction() : "";
        if (action.equals(UsbManager.ACTION_USB_DEVICE_ATTACHED)) {

        } else if (action.equals(UsbManager.ACTION_USB_DEVICE_DETACHED)) {

        }
    }
}