package com.kikatech.usb.driver;

import android.support.annotation.NonNull;

/**
 * Created by tianli on 17-11-6.
 */

public interface UsbAudioDriver {

    boolean open();

    void startRecording();

    int read(@NonNull byte[] audioData, int offsetInBytes, int sizeInBytes);

    void stopRecording();

    void close();
}
