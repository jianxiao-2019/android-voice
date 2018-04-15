package com.xiao.usbaudio;

import android.text.TextUtils;
import android.util.Log;

import com.kikatech.usb.driver.impl.KikaAudioDriver;
import com.kikatech.voice.core.debug.DebugUtil;
import com.kikatech.voice.util.log.Logger;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class AudioPlayBack {
    private static KikaAudioDriver sKikaAudioDriver;

    public static final int RAW_DATA_LENGTH_STEREO = 640;

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
        Logger.v("AudioPlayBack write len = " + len);
        if (mListener != null) {
            mListener.onWrite(len);
        }

        if (len == 0) {
            return;
        }
        if (sKikaAudioDriver != null) {
            sKikaAudioDriver.onData(decodedAudio, len);
        }

//        Logger.d("sFilePath = " + sFilePath + " output = " + output);
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

        sFilePath = DebugUtil.getDebugFilePath();
        Logger.d("AudioPlayBack setup sFilePath = " + sFilePath);
        if (!TextUtils.isEmpty(sFilePath)) {
            mRecording = getFile("_USB");
            Logger.d("AudioPlayBack setup mRecording = " + mRecording);
            try {
                output = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(mRecording, true)));
                Logger.d("AudioPlayBack setup output = " + output);
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

    public static File getFile(final String suffix) {
        if (TextUtils.isEmpty(sFilePath)) {
            return null;
        }

        File file = new File(sFilePath  + suffix);
        return file;
    }
}
