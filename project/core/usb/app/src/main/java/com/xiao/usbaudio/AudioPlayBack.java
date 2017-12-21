package com.xiao.usbaudio;

import android.util.Log;

import com.kikatech.usb.driver.impl.AudioBuffer;
import com.kikatech.voice.util.log.Logger;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class AudioPlayBack {
    private static AudioBuffer sAudioBuffer;

    // For debug
    private static long previousWrite = 0;
    private static String sTmpLog = "";
    private static int count = 0;

    public static void write(byte[] decodedAudio, int len) {
        if(Logger.DEBUG) {
            String msg = "AudioPlayBack write size = " + decodedAudio.length + " len = " + len;
            long diff = System.currentTimeMillis() - previousWrite;
            count++;
            if (diff > 2000 || !sTmpLog.equals(msg)) {
                sTmpLog = msg;
                msg += " (" + count + " same logs in " + diff + " ms)";
                Logger.d(msg);
                previousWrite = System.currentTimeMillis();
                count = 0;
            }
            //Logger.d("AudioPlayBack write size = " + decodedAudio.length + " len = " + len);
        }

        if (len == 0) {
            return;
        }
//        byte[] monoResult = new byte[len / 2];
//        for (int i = 0; i < monoResult.length; i += 2) {
//            monoResult[i] = decodedAudio[i * 2];
//            monoResult[i + 1] = decodedAudio[i * 2 + 1];
//        }
//        sAudioBuffer.write(monoResult, monoResult.length);
        sAudioBuffer.write(decodedAudio, len);

//        if (sFilePath != null) {
//            try {
//                for (int i = 0; i < len; i++) {
//                    output.writeByte(decodedAudio[i]);
//                }
//            } catch (IOException e) {
//                Log.e("Error writing file : ", e.getMessage());
//            } finally {
//                if (output != null) {
//                    try {
//                        output.flush();
//                    } catch (IOException e) {
//                        Log.e("Error writing file : ", e.getMessage());
//                    }
//                }
//            }
//        }
    }

    public static void setup(AudioBuffer audioBuffer) {
        sAudioBuffer = audioBuffer;

//        if (sFilePath != null) {
//            mRecording = getFile("raw");
//            try {
//                output = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(mRecording)));
//            } catch (IOException e) {
//                Log.e("Error writing file : ", e.getMessage());
//            }
//        }
    }

    public static void stop() {
        sAudioBuffer = null;
    }

    // For deubg
//    public static String sFilePath;
//    private static DataOutputStream output = null;
//    private static File mRecording;
//
//    private static File getFile(final String suffix) {
//        if (sFilePath == null) {
//            return null;
//        }
//
//        File file = new File(sFilePath + "_SRC");
//        Log.d("Ryan", "file = " + file + " exist = " + file.exists());
//        return file;
//    }
}
