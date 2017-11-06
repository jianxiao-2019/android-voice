package com.kikatech.usb.device;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.util.Log;

/**
 * Created by tianli on 17-11-6.
 */

public class UsbDeviceReceiver extends BroadcastReceiver {

    final static String TAG = "UsbDeviceReceiver";

    private static final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";
    private static final String ACTION_USB_ATTACHED = "android.hardware.usb.action.USB_DEVICE_ATTACHED";
    private static final String ACTION_USB_DETACHED = "android.hardware.usb.action.USB_DEVICE_DETACHED";

    @Override
    public void onReceive(Context context, Intent intent) {
        if(context == null || intent == null){
            return;
        }
        String action = intent.getAction();
        UsbDevice device;
        Log.v(TAG, "action = " + action);
        switch (action) {
            case ACTION_USB_ATTACHED:
                device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                onUsbAttached(device);
                break;
            case ACTION_USB_DETACHED:
                device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                onUsbDetached(device);
                break;
            case ACTION_USB_PERMISSION:
                device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                    if (device != null) {
                        onUsbPermissionGrant(device);
                    }
                } else {
                    Log.d(TAG, "permission denied for device " + device);
                }
                break;
        }
    }

    private void onUsbAttached(UsbDevice device){
        if(device != null){
        }
    }

    private void onUsbDetached(UsbDevice device){
        if(device != null){
        }
    }

    private void onUsbPermissionGrant(UsbDevice device){
        if(device != null){
        }
    }

}
