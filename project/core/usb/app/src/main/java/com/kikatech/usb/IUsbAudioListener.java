package com.kikatech.usb;

/**
 * Created by tianli on 17-11-6.
 */

public interface IUsbAudioListener {

    void onDeviceAttached(UsbAudioSource audioSource);

    void onDeviceDetached();

    void onDeviceError(int errorCode);
}
