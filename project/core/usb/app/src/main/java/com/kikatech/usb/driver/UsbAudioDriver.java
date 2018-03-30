package com.kikatech.usb.driver;

/**
 * Created by tianli on 17-11-6.
 */

public interface UsbAudioDriver {

    int RESULT_CONNECTION_FAIL = -2;
    int RESULT_FAIL = -1;
    int RESULT_MONO = 1;
    int RESULT_STEREO = 2;

    int open();

    void startRecording();

    void stopRecording();

    void close();

    void setOnDataListener(OnDataListener listener);

    interface OnDataListener {
        void onData(byte[] data, int length);
    }

    int checkVolumeState();

    int volumeUp();

    int volumeDown();

    int checkFwVersion();

    int checkDriverVersion();
}
