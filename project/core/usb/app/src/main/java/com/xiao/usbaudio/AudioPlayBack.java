package com.xiao.usbaudio;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Environment;
import android.text.format.Time;
import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.util.Log;

public class AudioPlayBack {
    private static final String TAG = "AudioPlayback";

    private static final int SAMPLE_RATE_HZ = 48000;

    private static AudioTrack track = null;

    private static final int samplingRates[] = {16000, 11025, 11000, 8000, 6000};
    public static int SAMPLE_RATE = 96000;//16000;
    private static AudioRecord mRecorder;
    private static File mRecording;
    private static short[] mBuffer;
    private static String audioFilePath;
    private boolean mIsRecording = false;
    private static String RECORD_WAV_PATH = Environment.getExternalStorageDirectory() + File.separator + "AudioRecord";
    private static DataOutputStream output = null;
    private static boolean enable_write = false;


    public static void write(byte[] decodedAudio, int len) {
        Log.d(TAG, "------- write " + decodedAudio.length);
        if (!enable_write)
            return;
        try {
            int readSize = len;//decodedAudio.length;
            for (int i = 0; i < readSize; i++) {
                if (enable_write)
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


    public static void setup() {
        Log.i(TAG, "Audio Playback");
        //initRecorder();
        enable_write = true;
        mRecording = getFile("raw");
        Log.d(TAG, "---path : " + mRecording.getAbsolutePath());
        try {
            output = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(mRecording)));
        } catch (IOException e) {
            Log.e("Error writing file : ", e.getMessage());
        }
    }


    public static void stop() {
        enable_write = false;
        try {
            output.close();
            File waveFile = getFile("wav");
            rawToWave(mRecording, waveFile);

        } catch (IOException e) {
            Log.e("Error close file : ", e.getMessage());
        }
    }

    private static void initRecorder() {
        SAMPLE_RATE = getValidSampleRates();
        int bufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE, AudioFormat.CHANNEL_IN_STEREO,
                AudioFormat.ENCODING_PCM_16BIT);
        mBuffer = new short[bufferSize];
        mRecorder = new AudioRecord(MediaRecorder.AudioSource.MIC, SAMPLE_RATE, AudioFormat.CHANNEL_IN_STEREO,
                AudioFormat.ENCODING_PCM_16BIT, bufferSize);
        new File(RECORD_WAV_PATH).mkdir();
    }

    /* Get Supported Sample Rate */
    public static int getValidSampleRates() {
        for (int rate : samplingRates) {
            int bufferSize = AudioRecord.getMinBufferSize(rate, AudioFormat.CHANNEL_CONFIGURATION_DEFAULT, AudioFormat.ENCODING_PCM_16BIT);
            if (bufferSize > 0) {
                return rate;
            }
        }
        return SAMPLE_RATE;
    }


    /* Start AudioRecording */
    public void recordWavStart() {
        mIsRecording = true;
        mRecorder.startRecording();
        mRecording = getFile("raw");
        startBufferedWrite(mRecording);
    }

    private void startBufferedWrite(final File file) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                DataOutputStream output = null;
                try {
                    output = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(file)));
                    while (mIsRecording) {
                        double sum = 0;
                        int readSize = mRecorder.read(mBuffer, 0, mBuffer.length);
                        for (int i = 0; i < readSize; i++) {
                            output.writeShort(mBuffer[i]);
                            sum += mBuffer[i] * mBuffer[i];
                        }
                        if (readSize > 0) {
                            final double amplitude = sum / readSize;
                        }
                    }
                } catch (IOException e) {
                    Log.e("Error writing file : ", e.getMessage());
                } finally {

                    if (output != null) {
                        try {
                            output.flush();
                        } catch (IOException e) {
                            Log.e("Error writing file : ", e.getMessage());
                        } finally {
                            try {
                                output.close();
                            } catch (IOException e) {
                                Log.e("Error writing file : ", e.getMessage());
                            }
                        }
                    }
                }
            }
        }).start();
    }


    /* Get file name */
    private static File getFile(final String suffix) {
        Time time = new Time();
        time.setToNow();
        audioFilePath = time.format("%Y%m%d%H%M%S");
        return new File(RECORD_WAV_PATH, time.format("%Y%m%d%H%M%S") + "." + suffix);
    }

    /* Converting RAW format To WAV Format*/
    private static void rawToWave(final File rawFile, final File waveFile) throws IOException {

        byte[] rawData = new byte[(int) rawFile.length()];
        DataInputStream input = null;
        try {
            input = new DataInputStream(new FileInputStream(rawFile));
            input.read(rawData);
        } finally {
            if (input != null) {
                input.close();
            }
        }
        DataOutputStream output = null;
        try {
            output = new DataOutputStream(new FileOutputStream(waveFile));
            // WAVE header
            writeString(output, "RIFF"); // chunk id
            writeInt(output, 36 + rawData.length); // chunk size
            writeString(output, "WAVE"); // format
            writeString(output, "fmt "); // subchunk 1 id
            writeInt(output, 16); // subchunk 1 size
            writeShort(output, (short) 1); // audio format (1 = PCM)
            writeShort(output, (short) 2); // number of channels
            writeInt(output, SAMPLE_RATE); // sample rate
            writeInt(output, SAMPLE_RATE * 4); // byte rate
            writeShort(output, (short) 2); // block align
            writeShort(output, (short) 16); // bits per sample
            writeString(output, "data"); // subchunk 2 id
            writeInt(output, rawData.length); // subchunk 2 size
            // Audio data (conversion big endian -> little endian)
            short[] shorts = new short[rawData.length / 2];
            ByteBuffer.wrap(rawData).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(shorts);
            ByteBuffer bytes = ByteBuffer.allocate(shorts.length * 2);
            for (short s : shorts) {
                bytes.putShort(s);
            }
            output.write(bytes.array());
        } finally {
            if (output != null) {
                output.close();
                rawFile.delete();
            }
        }
    }

    private static void writeShort(final DataOutputStream output, final short value) throws IOException {
        output.write(value >> 0);
        output.write(value >> 8);
    }

    private static void writeString(final DataOutputStream output, final String value) throws IOException {
        for (int i = 0; i < value.length(); i++) {
            output.write(value.charAt(i));
        }
    }

    private static void writeInt(final DataOutputStream output, final int value) throws IOException {
        output.write(value >> 0);
        output.write(value >> 8);
        output.write(value >> 16);
        output.write(value >> 24);
    }

}
