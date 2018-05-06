package com.kikatech.usb;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbAccessory;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.text.TextUtils;
import android.util.Log;

import com.kikatech.usb.util.DeviceUtil;

/**
 * Created by tianli on 17-11-6.
 */

public class UsbDeviceReceiver extends BroadcastReceiver {

    final static String TAG = "UsbDeviceReceiver";

    public static final String ACTION_USB_DEVICE_PERMISSION_GRANTED = "com.kikatech.usb.device.USB_PERMISSION";
    public static final String ACTION_USB_ACCESSORY_PERMISSION_GRANTED = "com.kikatech.usb.accessory.USB_PERMISSION";

    private UsbDeviceListener mDeviceListener;
    private UsbAccessoryListener mAccessoryListener;

    public UsbDeviceReceiver(UsbDeviceListener deviceListener,
                             UsbAccessoryListener accessoryListener) {
        mDeviceListener = deviceListener;
        mAccessoryListener = accessoryListener;
    }

    public void register(Context context) {
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_USB_DEVICE_PERMISSION_GRANTED);
        filter.addAction(ACTION_USB_ACCESSORY_PERMISSION_GRANTED);
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
        Log.v(TAG, "action = " + action);
        if (TextUtils.isEmpty(action)) {
            Log.e(TAG, "UsbDeviceReceiver receive an empty action.");
            return;
        }

        UsbDevice device;
        UsbAccessory accessory;
        switch (action) {
            case UsbManager.ACTION_USB_DEVICE_ATTACHED:
                device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                if (DeviceUtil.isKikaGoDevice(device)) { // we only provider audio device access
                    onUsbAttached(device);
                }
                break;
            case UsbManager.ACTION_USB_DEVICE_DETACHED:
                device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                if (DeviceUtil.isKikaGoDevice(device)) {
                    onUsbDetached(device);
                }
                break;
            case ACTION_USB_DEVICE_PERMISSION_GRANTED:
                device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)
                        && DeviceUtil.isKikaGoDevice(device)) {
                    onUsbPermissionGranted(device);
                } else {
                    Log.e(TAG, "permission denied for device " + device);
                }
                break;
            case UsbManager.ACTION_USB_ACCESSORY_ATTACHED:
                accessory = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                if (DeviceUtil.isKikaGoAccessory(accessory)) { // we only provider audio device access
                    onUsbAccessoryAttached(accessory);
                }
                break;
            case UsbManager.ACTION_USB_ACCESSORY_DETACHED:
                accessory = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                if (DeviceUtil.isKikaGoAccessory(accessory)) {
                    onUsbAccessoryDetached(accessory);
                }
                break;
            case ACTION_USB_ACCESSORY_PERMISSION_GRANTED:
                accessory = intent.getParcelableExtra(UsbManager.EXTRA_ACCESSORY);
                if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)
                        && DeviceUtil.isKikaGoAccessory(accessory)) {
                    onUsbAccessoryPermissionGranted(accessory);
                } else {
                    Log.e(TAG, "permission denied for accessory " + accessory);
                }
                break;
        }
    }

    private void onUsbAttached(UsbDevice device) {
        if (mDeviceListener != null) {
            mDeviceListener.onUsbAttached(device);
        }
    }

    private void onUsbDetached(UsbDevice device) {
        if (mDeviceListener != null) {
            mDeviceListener.onUsbDetached(device);
        }
    }

    private void onUsbPermissionGranted(UsbDevice device) {
        if (mDeviceListener != null) {
            mDeviceListener.onUsbPermissionGranted(device);
        }
    }

    private void onUsbAccessoryAttached(UsbAccessory accessory) {
        if (mAccessoryListener != null) {
            mAccessoryListener.onUsbAttached(accessory);
        }
    }

    private void onUsbAccessoryDetached(UsbAccessory accessory) {
        if (mAccessoryListener != null) {
            mAccessoryListener.onUsbDetached(accessory);
        }
    }

    private void onUsbAccessoryPermissionGranted(UsbAccessory accessory) {
        if (mAccessoryListener != null) {
            mAccessoryListener.onUsbPermissionGranted(accessory);
        }
    }

    interface UsbDeviceListener {

        void onUsbAttached(UsbDevice device);

        void onUsbDetached(UsbDevice device);

        void onUsbPermissionGranted(UsbDevice device);
    }

    interface UsbAccessoryListener {

        void onUsbAttached(UsbAccessory accessory);

        void onUsbDetached(UsbAccessory accessory);

        void onUsbPermissionGranted(UsbAccessory accessory);
    }
}
