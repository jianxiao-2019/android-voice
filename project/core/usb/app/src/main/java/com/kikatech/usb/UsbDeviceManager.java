package com.kikatech.usb;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbAccessory;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.support.annotation.NonNull;
import android.util.Log;

import com.kikatech.usb.util.DeviceUtil;
import com.kikatech.voice.util.log.Logger;

import java.util.HashMap;

/**
 * Created by tianli on 17-11-6.
 */

class UsbDeviceManager {

    private static final String TAG = "UsbDeviceManager";
    private Context mContext;

    private UsbDeviceReceiver mDeviceReceiver;
    private IUsbAudioDeviceListener mListener = null;

    interface IUsbAudioDeviceListener {

        void onDeviceAttached(UsbDevice device);

        void onDeviceDetached();

        void onAccessoryAttached(UsbAccessory accessory);

        void onAccessoryDetached();

        void onNoDevices();
    }

    public UsbDeviceManager(Context context, IUsbAudioDeviceListener listener) {
        mContext = context.getApplicationContext();
        mDeviceReceiver = new UsbDeviceReceiver(mDeviceListener, mAccessoryListener);
        mDeviceReceiver.register(context);
        mListener = listener;
    }

    public void scanDevices() {
        UsbManager manager = (UsbManager) mContext.getSystemService(Context.USB_SERVICE);
        if (manager == null) {
            mListener.onNoDevices();
            return;
        }

        if (scanUsbDevices(manager)) {
            return;
        }

        if (scanUsbAccessories(manager)) {
            return;
        }

        mListener.onNoDevices();
    }

    private boolean scanUsbDevices(UsbManager manager) {
        Logger.i("scanUsbDevices");
        HashMap<String, UsbDevice> allDeviceList = manager.getDeviceList();
        if (allDeviceList == null || allDeviceList.size() == 0) {
            return false;
        }

        Logger.i("scanUsbDevices allDeviceList.size = " + allDeviceList.values().size());
        for (UsbDevice device : allDeviceList.values()) {
            if (DeviceUtil.isKikaGoDevice(device)) {
                mDeviceListener.onUsbAttached(device);
                return true;
            }
        }

        return false;
    }

    private boolean scanUsbAccessories(@NonNull UsbManager manager) {
        Logger.i("scanUsbAccessories");
        UsbAccessory[] accessories = manager.getAccessoryList();
        if (accessories == null || accessories.length == 0) {
            return false;
        }

        Logger.i("scanUsbAccessories accessories.size = " + accessories.length);
        for (UsbAccessory accessory : accessories) {
            if (DeviceUtil.isKikaGoAccessory(accessory)) {
                mAccessoryListener.onUsbAttached(accessory);
                return true;
            }
        }

        return false;
    }

    private UsbDeviceReceiver.UsbDeviceListener mDeviceListener
            = new UsbDeviceReceiver.UsbDeviceListener() {
        @Override
        public void onUsbAttached(UsbDevice device) {
            Log.d(TAG, "onUsbAttached device: " + device);
            if (hasPermission(device)) {
                mListener.onDeviceAttached(device);
            } else {
                requestPermission(device);
            }
        }

        @Override
        public void onUsbDetached(UsbDevice device) {
            mListener.onDeviceDetached();
        }

        @Override
        public void onUsbPermissionGranted(UsbDevice device) {
            mListener.onDeviceAttached(device);
        }
    };

    private UsbDeviceReceiver.UsbAccessoryListener mAccessoryListener
            = new UsbDeviceReceiver.UsbAccessoryListener() {
        @Override
        public void onUsbAttached(UsbAccessory accessory) {
            Logger.i("onUsbAttached accessory = " + accessory);
            if (hasPermission(accessory)) {
                mListener.onAccessoryAttached(accessory);
            } else {
                requestPermission(accessory);
            }
        }

        @Override
        public void onUsbDetached(@NonNull UsbAccessory accessory) {
            mListener.onAccessoryDetached();
        }

        @Override
        public void onUsbPermissionGranted(@NonNull UsbAccessory accessory) {
            mListener.onAccessoryAttached(accessory);
        }
    };

    private boolean hasPermission(UsbDevice device) {
        UsbManager manager = (UsbManager) mContext.getSystemService(Context.USB_SERVICE);
        return manager != null && manager.hasPermission(device);
    }

    private boolean hasPermission(UsbAccessory accessory) {
        UsbManager manager = (UsbManager) mContext.getSystemService(Context.USB_SERVICE);
        return manager != null && manager.hasPermission(accessory);
    }

    private void requestPermission(UsbDevice device) {
        PendingIntent intent = PendingIntent.getBroadcast(mContext, 0,
                new Intent(UsbDeviceReceiver.ACTION_USB_DEVICE_PERMISSION_GRANTED), 0);
        UsbManager manager = (UsbManager) mContext.getSystemService(Context.USB_SERVICE);
        if (device != null && manager != null && intent != null) {
            manager.requestPermission(device, intent);
        } else {
            Log.e(TAG, "requestPermission exception.");
        }
    }

    private void requestPermission(UsbAccessory accessory) {
        PendingIntent intent = PendingIntent.getBroadcast(mContext, 0,
                new Intent(UsbDeviceReceiver.ACTION_USB_ACCESSORY_PERMISSION_GRANTED), 0);
        UsbManager manager = (UsbManager) mContext.getSystemService(Context.USB_SERVICE);
        if (accessory != null && manager != null && intent != null) {
            manager.requestPermission(accessory, intent);
        } else {
            Log.e(TAG, "requestPermission exception.");
        }
    }
}
