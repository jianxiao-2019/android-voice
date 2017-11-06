package com.kikatech.usb;

/**
 * Created by tianli on 17-11-6.
 */

public interface IUsbAudioListener {

    void onDeviceAttached(UsbAudioRecord audioRecord);

    void onDeviceDetached();
}
