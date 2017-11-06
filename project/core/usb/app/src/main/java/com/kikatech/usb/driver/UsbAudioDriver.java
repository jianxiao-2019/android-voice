package com.kikatech.usb.driver;

import android.hardware.usb.UsbDevice;
import android.support.annotation.NonNull;

/**
 * Created by tianli on 17-11-6.
 */

public interface UsbAudioDriver {

    boolean open();

    void startRecording();

    void read(@NonNull byte[] audioData, int offsetInBytes, int sizeInBytes);

    void stopRecording();

    void close();
}
