package com.xiao.usbaudio;

import android.text.TextUtils;
import android.util.Log;

import com.kikatech.usb.driver.impl.KikaAudioDriver;
import com.kikatech.voice.util.log.Logger;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class AudioPlayBack {
    private static KikaAudioDriver sKikaAudioDriver;
    // For debug
    private static long previousWrite = 0;
    private static String sTmpLog = "";
    private static int count = 0;

    private static OnAudioPlayBackWriteListener mListener;

    // For check the hardware issue : audio source is mono or stereo.
    public interface OnAudioPlayBackWriteListener {
        void onWrite(int len);
    }

    public static void write(byte[] decodedAudio, int len) {
        if (mListener != null) {
            mListener.onWrite(len);
        }

        if (len == 0) {
            return;
        }
        sKikaAudioDriver.onData(decodedAudio, len);

        if (sFilePath != null) {
            try {
                for (int i = 0; i < len; i++) {
                    output.writeByte(decodedAudio[i]);
                }
            } catch (IOException e) {
                Log.e("Error writing file : ", e.getMessage());
            } finally {
                if (output != null) {
                    try {
                        output.flush();
                    } catch (IOException e) {
                        Log.e("Error writing file : ", e.getMessage());
                    }
                }
            }
        }
    }

    public static void setup(KikaAudioDriver kikaAudioDriver) {
        sKikaAudioDriver = kikaAudioDriver;

        if (sFilePath != null) {
            mRecording = getFile("raw");
            try {
                output = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(mRecording)));
            } catch (IOException e) {
                Log.e("Error writing file : ", e.getMessage());
            }
        }
    }

    public static void stop() {
        sKikaAudioDriver = null;
    }

    public static void setListener(OnAudioPlayBackWriteListener listener) {
        mListener = listener;
    }

    // For deubg
    public static String sFilePath;
    private static DataOutputStream output = null;
    private static File mRecording;

    private static File getFile(final String suffix) {
        if (TextUtils.isEmpty(sFilePath)) {
            return null;
        }

        File file = new File(sFilePath  + "_USB");
        return file;
    }
}
