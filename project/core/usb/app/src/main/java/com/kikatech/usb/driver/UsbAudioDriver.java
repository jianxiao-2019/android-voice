package com.kikatech.usb.driver;

/**
 * Created by tianli on 17-11-6.
 */

public interface UsbAudioDriver {

    boolean open();

    void startRecording();

    void stopRecording();

    void close();

    void setOnDataListener(OnDataListener listener);

    interface OnDataListener {
        void onData(byte[] data, int length);
    }
}
