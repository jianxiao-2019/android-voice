package com.kikatech.usb.driver;

import android.hardware.usb.UsbAccessory;
import android.hardware.usb.UsbDevice;
import android.text.TextUtils;

import com.kikatech.usb.eventbus.UsbEvent;
import com.kikatech.usb.util.DeviceUtil;
import com.kikatech.usb.util.LogUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

/**
 * Created by tianli on 17-11-6.
 */

public class UsbDeviceReceiver {

    final static String TAG = "UsbDeviceReceiver";

    private UsbDeviceListener mDeviceListener;
    private UsbAccessoryListener mAccessoryListener;

    UsbDeviceReceiver(UsbDeviceListener deviceListener,
                      UsbAccessoryListener accessoryListener) {
        mDeviceListener = deviceListener;
        mAccessoryListener = accessoryListener;
    }

    public void register() {
        EventBus.getDefault().register(this);
    }


    /**
     * <p>Reflection subscriber method used by EventBus,
     * <p>do not remove this except the subscriber is no longer needed.
     *
     * @param event event from {@link com.kikatech.usb.driver.receiver.UsbSysIntentProcessor}
     */
    @SuppressWarnings("unused")
    @Subscribe(threadMode = ThreadMode.POSTING)
    public void onUsbEvent(UsbEvent event) {
        if (event == null) {
            if (LogUtil.DEBUG) {
                LogUtil.logw(TAG, "invalid UsbEvent");
            }
            return;
        }

        String action = event.getAction();
        if (TextUtils.isEmpty(action)) {
            if (LogUtil.DEBUG) {
                LogUtil.logw(TAG, "invalid UsbEvent.action");
            }
            return;
        }

        UsbDevice device;
        UsbAccessory accessory;

        switch (action) {
            case UsbEvent.ACTION_USB_DEVICE_ATTACHED:
                device = event.getExtras().getParcelable(UsbEvent.PARAM_USB_DEVICE);
                if (DeviceUtil.isKikaGoDevice(device)) {
                    onUsbAttached(device);
                }
                break;
            case UsbEvent.ACTION_USB_DEVICE_DETACHED:
                device = event.getExtras().getParcelable(UsbEvent.PARAM_USB_DEVICE);
                if (DeviceUtil.isKikaGoDevice(device)) {
                    onUsbDetached(device);
                }
                break;
            case UsbEvent.ACTION_USB_DEVICE_PERMISSION_GRANTED:
                device = event.getExtras().getParcelable(UsbEvent.PARAM_USB_DEVICE);
                if (DeviceUtil.isKikaGoDevice(device)) {
                    onUsbPermissionGranted(device);
                }
                break;
            case UsbEvent.ACTION_USB_ACCESSORY_ATTACHED:
                accessory = event.getExtras().getParcelable(UsbEvent.PARAM_USB_ACCESSORY);
                if (DeviceUtil.isKikaGoAccessory(accessory)) {
                    onUsbAccessoryAttached(accessory);
                }
                break;
            case UsbEvent.ACTION_USB_ACCESSORY_DETACHED:
                accessory = event.getExtras().getParcelable(UsbEvent.PARAM_USB_ACCESSORY);
                if (DeviceUtil.isKikaGoAccessory(accessory)) {
                    onUsbAccessoryDetached(accessory);
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


    interface UsbDeviceListener {

        void onUsbAttached(UsbDevice device);

        void onUsbDetached(UsbDevice device);

        void onUsbPermissionGranted(UsbDevice device);
    }

    interface UsbAccessoryListener {

        void onUsbAttached(UsbAccessory accessory);

        void onUsbDetached(UsbAccessory accessory);
    }
}
