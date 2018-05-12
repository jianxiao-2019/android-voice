package com.xiao.usbaudio;

import com.kikatech.usb.KikaGoDeviceDataSource;
import com.kikatech.voice.util.log.Logger;

public class AudioPlayBack {

    private static KikaGoDeviceDataSource sKikaGoDeviceDataSource;
    private static OnAudioPlayBackWriteListener mListener;

    // For check the hardware issue : audio source is mono or stereo.
    public interface OnAudioPlayBackWriteListener {
        void onWrite(int len);
    }

    public static void write(byte[] decodedAudio, int len) {
        Logger.v("AudioPlayBack write len = " + len
                + " sKikaGoDeviceDataSource = " + sKikaGoDeviceDataSource);
        if (mListener != null) {
            mListener.onWrite(len);
        }

        if (len == 0) {
            return;
        }
        if (sKikaGoDeviceDataSource != null) {
            sKikaGoDeviceDataSource.onData(decodedAudio, len);
        }
    }

    public static void setup(KikaGoDeviceDataSource kikaAudioDriver) {
        Logger.d("AudioPlayBack setup sKikaGoDeviceDataSource = " + kikaAudioDriver);
        sKikaGoDeviceDataSource = kikaAudioDriver;
    }

    public static void stop() {
        Logger.d("AudioPlayBack stop sKikaGoDeviceDataSource = " + sKikaGoDeviceDataSource);
        sKikaGoDeviceDataSource = null;
    }

    public static void setListener(OnAudioPlayBackWriteListener listener) {
        mListener = listener;
    }
}
