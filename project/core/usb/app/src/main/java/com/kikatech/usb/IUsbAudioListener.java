package com.kikatech.usb;

/**
 * Created by tianli on 17-11-6.
 */

public interface IUsbAudioListener {

    int ERROR_NO_DEVICES = 0;
    int ERROR_DRIVER_INIT_FAIL = 1;

    void onDeviceAttached(UsbAudioSource audioSource);

    void onDeviceDetached();

    void onDeviceError(int errorCode);
}
