package com.kikatech.usb;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.util.Log;

import java.util.HashMap;

/**
 * Created by tianli on 17-11-6.
 */

class UsbDeviceManager {

    private static final String TAG = "UsbDeviceManager";
    private Context mContext;
    private UsbDevice mDevice;
    private UsbDeviceReceiver mDeviceReceiver;
    private IUsbAudioDeviceListener mListener = null;

    public UsbDeviceManager(Context context, IUsbAudioDeviceListener listener) {
        mContext = context.getApplicationContext();
        mDeviceReceiver = new UsbDeviceReceiver(mDeviceListener);
        mDeviceReceiver.register(context);
        mListener = listener;
    }

    public void setReqPermissionOnReceiver(boolean reqPermissionOnReceiver) {
        if (mDeviceReceiver != null) {
            mDeviceReceiver.setReqPermission(reqPermissionOnReceiver);
        }
    }

    public boolean hasPermission(UsbDevice device) {
        UsbManager manager = (UsbManager) mContext.getSystemService(Context.USB_SERVICE);
        return manager.hasPermission(device);
    }

    public void requestPermission(UsbDevice device, BroadcastReceiver receiver) {
        // Register for permission
        PendingIntent intent = PendingIntent.getBroadcast(mContext, 0,
                new Intent(UsbDeviceReceiver.ACTION_USB_PERMISSION_GRANTED), 0);
        if (device != null && intent != null) {
            IntentFilter filter = new IntentFilter(UsbDeviceReceiver.ACTION_USB_PERMISSION_GRANTED);
            mContext.registerReceiver(receiver, filter);
            UsbManager manager = (UsbManager) mContext.getSystemService(Context.USB_SERVICE);
            manager.requestPermission(device, intent);
        } else {
            Log.e(TAG, "requestPermission exception.");
        }
    }

    public void scanDevices() {
        UsbManager manager = (UsbManager) mContext.getSystemService(Context.USB_SERVICE);
        if (manager == null) {
            mListener.onNoDevices();
            return;
        }
        HashMap<String, UsbDevice> deviceList = manager.getDeviceList();
        if (deviceList == null || deviceList.size() == 0) {
            mListener.onNoDevices();
            return;
        }
        for (UsbDevice device : deviceList.values()) {
            mDeviceListener.onUsbAttached(device, true);
        }
    }

    private boolean isAudioDevice(UsbDevice device) {
        if (device != null && device.getInterfaceCount() > 0) {
            UsbInterface usbInterface = device.getInterface(0);
            Log.d(TAG, "Audio UsbInterface : " + usbInterface.getInterfaceClass());
            if (usbInterface.getInterfaceClass() == UsbConstants.USB_CLASS_AUDIO) {
                return true;
            }
        }
        return false;
    }

    private UsbDeviceReceiver.UsbDeviceListener mDeviceListener = new UsbDeviceReceiver.UsbDeviceListener() {
        @Override
        public void onUsbAttached(UsbDevice device, boolean reqPermission) {
            if (isAudioDevice(device)) {
                Log.d(TAG, "Audio class device: " + device);
                Log.d(TAG, "Audio class device name: " + device.getDeviceName());
                if (hasPermission(device)) {
                    mDevice = device;
                    mListener.onDeviceAttached(mDevice);
                } else if (reqPermission) {
                    requestPermission(device, mDeviceReceiver);
                }
            }
        }

        @Override
        public void onUsbDetached(UsbDevice device) {
            if (device != null && mDevice != null) {
                int detachedVendorId = device.getVendorId();
                int attachedVendorId = mDevice.getVendorId();
                int detachedProductId = device.getProductId();
                int attachedProductId = mDevice.getProductId();
                Log.d(TAG, "detachedDeviceVendorId: " + detachedVendorId);
                Log.d(TAG, "attachedDeviceVendorId: " + attachedVendorId);
                Log.d(TAG, "detachedProductId: " + detachedProductId);
                Log.d(TAG, "attachedProductId: " + attachedProductId);
                // TODO: 17-11-18 handle usb detach
                if (detachedVendorId == attachedVendorId && detachedProductId == attachedProductId) {
                    mDevice = null;
                    mListener.onDeviceDetached();
                }
            }
        }

        @Override
        public void onUsbPermissionGranted(UsbDevice device) {
            if (device != null) {
                mDevice = device;
                mListener.onDeviceAttached(mDevice);
            }
        }
    };

    interface IUsbAudioDeviceListener {

        void onDeviceAttached(UsbDevice device);

        void onDeviceDetached();

        void onNoDevices();
    }
}
