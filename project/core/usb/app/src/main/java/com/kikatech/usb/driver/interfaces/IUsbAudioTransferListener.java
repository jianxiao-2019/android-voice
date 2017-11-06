package com.kikatech.usb.driver.interfaces;

/**
 * @author SkeeterWang Created on 2017/10/26.
 */
public interface IUsbAudioTransferListener {
    void onAudioTransferStart();

    void onAudioTransferStop(String filePath);

    void onAudioTransferBufferResult(short[] data);
}
