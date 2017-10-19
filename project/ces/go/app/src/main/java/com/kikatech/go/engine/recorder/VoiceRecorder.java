package com.kikatech.go.engine.recorder;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.media.audiofx.AcousticEchoCanceler;

import com.kikatech.go.util.log.Logger;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by ryanlin on 06/10/2017.
 */

public class VoiceRecorder {

    public static final int STATUS_SUCCESS = 0;
    public static final int STATUS_RECORDER_INIT_FAIL = 1;
    public static final int STATUS_RECORDER_RESTART = 2;

    public final static int AUDIO_INPUT = MediaRecorder.AudioSource.VOICE_COMMUNICATION;
    public final static int AUDIO_SAMPLE_RATE = 16000;

    private final int mBufferSizeInBytes;

    private AudioRecord mAudioRecord;
    private AcousticEchoCanceler mCanceler;

    private final VoiceDetector mVoiceDetector;

    private AudioRecordThread mAudioRecordThread;

    private final Object mSyncObj = new Object();

    public VoiceRecorder(VoiceDetectorListener listener) {
        mBufferSizeInBytes = AudioRecord.getMinBufferSize(
                AUDIO_SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
        mVoiceDetector = new VoiceDetector(listener);
    }

    private AudioRecord createAudioRecord() {
        if (mBufferSizeInBytes <= 0) {
            return null;
        }
        try {
            return new AudioRecord(AUDIO_INPUT, AUDIO_SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_16BIT, mBufferSizeInBytes);
        } catch (Exception e) {
            return null;
        }
    }

    public int startRecording() {
        synchronized (mSyncObj) {
            if (mAudioRecord != null) {
                mAudioRecord.stop();
                mAudioRecord.release();
            }
            mAudioRecord = createAudioRecord();
            if (mAudioRecord == null || mAudioRecord.getState() != AudioRecord.STATE_INITIALIZED) {
                return STATUS_RECORDER_INIT_FAIL;
            }

            initAec(mAudioRecord);
            mAudioRecord.startRecording();
        }

        if (mAudioRecordThread != null) {
            mAudioRecordThread.stop();
        }
        mAudioRecordThread = new AudioRecordThread();
        new Thread(mAudioRecordThread).start();
        return STATUS_SUCCESS;
    }

    public int stopRecording() {
        Logger.d("VoiceRecorder stopRecording mAudioRecordThread = " + mAudioRecordThread);
        if (mAudioRecordThread != null) {
            mAudioRecordThread.stop();
            mAudioRecordThread = null;
        }
        if (mCanceler != null) {
            mCanceler.setEnabled(false);
            mCanceler.release();
            mCanceler = null;
        }
        synchronized (mSyncObj) {
            if (mAudioRecord != null) {
                mAudioRecord.stop();
                mAudioRecord.release();
                mAudioRecord = null;
            }
        }

        return STATUS_SUCCESS;
    }

    public void setDebugFilePath(String filePath) {
        mVoiceDetector.setDebugFileName(filePath);
    }

    private void initAec(AudioRecord audioRecord) {
        if (AcousticEchoCanceler.isAvailable()) {
            mCanceler = AcousticEchoCanceler.create(audioRecord.getAudioSessionId());
            if (mCanceler != null) {
                mCanceler.setEnabled(true);
                Logger.d("VoiceRecorder initAec mCanceler.enable = " + mCanceler.getEnabled());
            }
        }
    }

    private class AudioRecordThread implements Runnable {

        private static final int S_BYTE_LEN = (int) (4096 * 1.5);//vad中的输入是一段4096字节的音频

        private AtomicBoolean mIsRunning = new AtomicBoolean(false);

        private byte[] mBuf = new byte[S_BYTE_LEN];
        private int mBufLen = 0;

        public void stop() {
            Logger.d("AudioRecordThread stop");
            mIsRunning.set(false);
        }

        @Override
        public void run() {
            prepare();
            record();
            release();
        }

        private void prepare() {
            Logger.i("AudioRecordThread prepare");
            mIsRunning.set(true);
            mVoiceDetector.startDetecting();
        }

        private void record() {
            Logger.i("AudioRecordThread record");
            byte[] audioData = new byte[mBufferSizeInBytes];
            int readSize;
            while (mIsRunning.get()) {
                synchronized (mSyncObj) {
                    if (mAudioRecord != null) {
                        readSize = mAudioRecord.read(audioData, 0, mBufferSizeInBytes);
                        if (AudioRecord.ERROR_INVALID_OPERATION != readSize /*&& fos != null*/) {
                            copy(audioData, readSize);
                        }
                    }
                }
            }
        }

        private void release() {
            Logger.i("AudioRecordThread release");
            if (mBufLen > 0) {
                byte[] lastData = Arrays.copyOf(mBuf, mBufLen);
                Logger.d("VoiceRecorder release lastData.length = " + lastData.length);
                mVoiceDetector.addData(lastData);
            }
            mVoiceDetector.stopDetecting();
        }

        private void copy(byte[] audioData, int readSize) {
            int tempLen = readSize;
            int tempIdx = 0;
            int length;
            while (tempLen + mBufLen > S_BYTE_LEN) {
                length = S_BYTE_LEN - mBufLen;
                tempLen = tempLen - length;
                System.arraycopy(audioData, tempIdx, mBuf, mBufLen, length);
                tempIdx += length;
                mVoiceDetector.addData(mBuf);
                mBufLen = 0;
            }
            if (tempLen + mBufLen <= S_BYTE_LEN) {
                System.arraycopy(audioData, tempIdx, mBuf, mBufLen, tempLen);
                mBufLen += tempLen;
            }
        }
    }
}
