package com.kikatech.usb.driver.impl;

import android.content.Context;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.support.annotation.NonNull;
import android.util.Log;

import com.extreamsd.usbtester.USBControl;
import com.kikatech.usb.driver.UsbAudioDriver;

/**
 * Created by tianli on 17-11-6.
 */

public class ExtreamAudioDriver implements UsbAudioDriver {

    private final static String TAG = "ExtreamAudioDriver";

    private USBControl mUsbControl = null;
    private UsbDeviceConnection mConnection = null;
    private UsbDevice mDevice;
    private Context mContext;

    public ExtreamAudioDriver(Context context) {
        mContext = context.getApplicationContext();
    }

    @Override
    public boolean open(UsbDevice device) {
        mDevice = device;
        if (mConnection != null) {
            int fileDescriptor = mConnection.getFileDescriptor();

            boolean initUSBOk;
            if (android.os.Build.VERSION.SDK_INT >= 24) {
                initUSBOk = mUsbControl.initUSBDeviceByName(fileDescriptor, device.getDeviceName(), device.getProductId(),
                        device.getVendorId(), mConnection.getRawDescriptors(), mConnection.getRawDescriptors().length);
            } else {
                initUSBOk = mUsbControl.initUSBDevice(fileDescriptor, device.getProductId(), device.getVendorId());
            }
            if (!initUSBOk) {
                // TODO: 17-11-6 handle init error
            } else {
                // TODO: 17-11-6 handle init success
            }
        } else {
            Log.e(TAG, "Failed to open USB device");
        }
        return false;
    }

    @Override
    public void read(@NonNull byte[] audioData, int offsetInBytes, int sizeInBytes) {

    }

    @Override
    public void startRecording() {
    }

    @Override
    public void stopRecording() {
    }

    @Override
    public void close() {

    }
}
