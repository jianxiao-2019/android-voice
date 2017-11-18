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

public class ExtreamAudioDriver extends UsbHostAudioDriver {

    private final static String TAG = "ExtreamAudioDriver";

    private USBControl mUsbControl = null;

    public ExtreamAudioDriver(Context context, UsbDevice device) {
        super(context, device);
    }

    @Override
    public boolean open() {
        if (openConnection()) {
            int fileDescriptor = mConnection.getFileDescriptor();
            boolean initUSBOk;
            if (android.os.Build.VERSION.SDK_INT >= 24) {
                initUSBOk = mUsbControl.initUSBDeviceByName(fileDescriptor, mDevice.getDeviceName(), mDevice.getProductId(),
                        mDevice.getVendorId(), mConnection.getRawDescriptors(), mConnection.getRawDescriptors().length);
            } else {
                initUSBOk = mUsbControl.initUSBDevice(fileDescriptor, mDevice.getProductId(), mDevice.getVendorId());
            }
            if (!initUSBOk) {
                // TODO: 17-11-6 handle init error
            } else {
                // TODO: 17-11-6 handle init success
            }
            return true;
        }
        Log.e(TAG, "Failed to open USB device");
        return false;
    }

    @Override
    public int read(@NonNull byte[] audioData, int offsetInBytes, int sizeInBytes) {
        return 0;
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
