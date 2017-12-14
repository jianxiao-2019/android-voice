package com.kikatech.usb;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.util.Log;

/**
 * Created by tianli on 17-11-6.
 */

class UsbDeviceReceiver extends BroadcastReceiver {

    final static String TAG = "UsbDeviceReceiver";

    public static final String ACTION_USB_PERMISSION_GRANTED = "com.kikatech.usb.USB_PERMISSION";

    private UsbDeviceListener mListener;

    public UsbDeviceReceiver(UsbDeviceListener l) {
        mListener = l;
    }

    public void register(Context context) {
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_USB_PERMISSION_GRANTED);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        context.registerReceiver(this, filter);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (context == null || intent == null) {
            return;
        }
        String action = intent.getAction();
        UsbDevice device;
        Log.v(TAG, "action = " + action);
        switch (action) {
            case UsbManager.ACTION_USB_DEVICE_ATTACHED:
                device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                onUsbAttached(device);
                break;
            case UsbManager.ACTION_USB_DEVICE_DETACHED:
                device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                onUsbDetached(device);
                break;
            case ACTION_USB_PERMISSION_GRANTED:
                device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                    if (device != null) {
                        onUsbPermissionGranted(device);
                    }
                } else {
                    Log.d(TAG, "permission denied for device " + device);
                }
                break;
        }
    }

    private void onUsbAttached(UsbDevice device) {
        mListener.onUsbAttached(device);
    }

    private void onUsbDetached(UsbDevice device) {
        mListener.onUsbDetached(device);
    }

    private void onUsbPermissionGranted(UsbDevice device) {
        mListener.onUsbPermissionGranted(device);
    }

    interface UsbDeviceListener {

        void onUsbAttached(UsbDevice device);

        void onUsbDetached(UsbDevice device);

        void onUsbPermissionGranted(UsbDevice device);
    }
}