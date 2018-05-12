package com.kikatech.usb;

import com.kikatech.usb.datasource.KikaGoVoiceSource;

/**
 * Created by tianli on 17-11-6.
 */

public interface IUsbAudioListener {

    int ERROR_NO_DEVICES = 0;
    int ERROR_DRIVER_CONNECTION_FAIL = 1;
    int ERROR_DRIVER_MONO = 2;

    void onDeviceAttached(KikaGoVoiceSource audioSource);

    void onDeviceDetached();

    void onDeviceError(int errorCode);
}
