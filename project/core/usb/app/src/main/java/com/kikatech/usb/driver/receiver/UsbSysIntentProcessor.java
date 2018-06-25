package com.kikatech.usb.driver.receiver;

import android.content.Intent;
import android.hardware.usb.UsbAccessory;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.text.TextUtils;

import com.kikatech.usb.eventbus.UsbEvent;
import com.kikatech.voice.util.log.Logger;

/**
 * @author SkeeterWang Created on 2018/6/25.
 */

class UsbSysIntentProcessor {
    private static final String TAG = "UsbSysIntentProcessor";

    static void processIntent(String SUB_TAG, Intent intent) {
        if (intent == null) {
            if (Logger.DEBUG) {
                Logger.w(TAG, String.format("[%s] invalid intent", SUB_TAG));
            }
            return;
        }
        String action = intent.getAction();
        if (TextUtils.isEmpty(action)) {
            if (Logger.DEBUG) {
                Logger.w(TAG, String.format("[%s] invalid action", SUB_TAG));
            }
            return;
        }

        if (Logger.DEBUG) {
            Logger.i(TAG, String.format("[%s] action: %s", SUB_TAG, action));
        }

        switch (action) {
            case UsbManager.ACTION_USB_DEVICE_ATTACHED:
                onUsbDeviceAttached(intent, SUB_TAG);
                break;
            case UsbManager.ACTION_USB_DEVICE_DETACHED:
                onUsbDeviceDetached(intent, SUB_TAG);
                break;

            case UsbSysReceiver.ACTION_USB_DEVICE_PERMISSION_GRANTED:
                onUsbDevicePermissionGranted(intent, SUB_TAG);
                break;

            case UsbManager.ACTION_USB_ACCESSORY_ATTACHED:
                onUsbAccessoryAttached(intent, SUB_TAG);
                break;
            case UsbManager.ACTION_USB_ACCESSORY_DETACHED:
                onUsbAccessoryDetached(intent, SUB_TAG);
                break;
        }
    }

    private static void onUsbDeviceAttached(Intent intent, String SUB_TAG) {
        UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
        if (device == null) {
            if (Logger.DEBUG) {
                Logger.w(TAG, String.format("[%s] invalid usb device", SUB_TAG));
            }
            return;
        }
        if (Logger.DEBUG) {
            Logger.v(TAG, String.format("[%s] device: %s", SUB_TAG, device.toString()));
        }
        UsbEvent event = new UsbEvent(UsbEvent.ACTION_USB_DEVICE_ATTACHED);
        event.putExtra(UsbEvent.PARAM_USB_DEVICE, device);
        event.send();
    }

    private static void onUsbDeviceDetached(Intent intent, String SUB_TAG) {
        UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
        if (device == null) {
            if (Logger.DEBUG) {
                Logger.w(TAG, String.format("[%s] invalid usb device", SUB_TAG));
            }
            return;
        }
        if (Logger.DEBUG) {
            Logger.v(TAG, String.format("[%s] device: %s", SUB_TAG, device.toString()));
        }
        UsbEvent event = new UsbEvent(UsbEvent.ACTION_USB_DEVICE_DETACHED);
        event.putExtra(UsbEvent.PARAM_USB_DEVICE, device);
        event.send();
    }

    private static void onUsbDevicePermissionGranted(Intent intent, String SUB_TAG) {
        UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
        if (device == null) {
            if (Logger.DEBUG) {
                Logger.w(TAG, String.format("[%s] invalid usb device", SUB_TAG));
            }
            return;
        }
        if (Logger.DEBUG) {
            Logger.v(TAG, String.format("[%s] device: %s", SUB_TAG, device.toString()));
        }
        UsbEvent event = new UsbEvent(UsbEvent.ACTION_USB_DEVICE_PERMISSION_GRANTED);
        event.putExtra(UsbEvent.PARAM_USB_DEVICE, device);
        event.send();
    }

    private static void onUsbAccessoryAttached(Intent intent, String SUB_TAG) {
        UsbAccessory accessory = intent.getParcelableExtra(UsbManager.EXTRA_ACCESSORY);
        if (accessory == null) {
            if (Logger.DEBUG) {
                Logger.w(TAG, String.format("[%s] invalid accessory device", SUB_TAG));
            }
            return;
        }
        if (Logger.DEBUG) {
            Logger.v(TAG, String.format("[%s] accessory: %s", SUB_TAG, accessory.toString()));
        }
        UsbEvent event = new UsbEvent(UsbEvent.ACTION_USB_ACCESSORY_ATTACHED);
        event.putExtra(UsbEvent.PARAM_USB_ACCESSORY, accessory);
        event.send();
    }

    private static void onUsbAccessoryDetached(Intent intent, String SUB_TAG) {
        UsbAccessory accessory = intent.getParcelableExtra(UsbManager.EXTRA_ACCESSORY);
        if (accessory == null) {
            if (Logger.DEBUG) {
                Logger.w(TAG, String.format("[%s] invalid accessory device", SUB_TAG));
            }
            return;
        }
        if (Logger.DEBUG) {
            Logger.v(TAG, String.format("[%s] accessory: %s", SUB_TAG, accessory.toString()));
        }
        UsbEvent event = new UsbEvent(UsbEvent.ACTION_USB_ACCESSORY_DETACHED);
        event.putExtra(UsbEvent.PARAM_USB_ACCESSORY, accessory);
        event.send();
    }
}
