package com.kikatech.voice.core.recorder;

import com.kikatech.voice.core.framework.IDataPath;
import com.kikatech.voice.util.log.Logger;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by tianli on 17-10-28.
 */

public class VoiceRecorder {

    private final IDataPath mDataPath;
    private final IVoiceSource mVoiceSource;

    private AudioRecordThread mAudioRecordThread;

    public VoiceRecorder(IVoiceSource voiceSource, IDataPath dataPath) {
        mVoiceSource = voiceSource;
        mDataPath = dataPath;
    }

    public void start() {
        Logger.d("VoiceRecorder start mAudioRecordThread = " + mAudioRecordThread);
        if (mAudioRecordThread != null) {
            mAudioRecordThread.stop();
        }
        mAudioRecordThread = new AudioRecordThread();
        new Thread(mAudioRecordThread).start();
    }

    public void stop() {
        Logger.d("VoiceRecorder stopRecording mAudioRecordThread = " + mAudioRecordThread);
        if (mAudioRecordThread != null) {
            mAudioRecordThread.stop();
            mAudioRecordThread = null;
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
            // Sync Snowboy sample code
            android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_AUDIO);

            prepare();
            record();
            release();
        }

        private void prepare() {
            Logger.i("AudioRecordThread prepare");
            mIsRunning.set(true);
            mVoiceSource.start();
        }

        private void record() {
            Logger.i("AudioRecordThread record bufferSize = " + mVoiceSource.getBufferSize());
            byte[] audioData = new byte[mVoiceSource.getBufferSize()];
            int readSize;
            while (mIsRunning.get()) {
                readSize = mVoiceSource.read(audioData, 0, mVoiceSource.getBufferSize());
//                if (AudioRecord.ERROR_INVALID_OPERATION != readSize /*&& fos != null*/) {
                if (readSize > 0) {
                    copy(audioData, readSize);
                } else {
                    Logger.e("[AudioRecordThread][Err] readSize = " + readSize);
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        private void release() {
            Logger.i("AudioRecordThread release");
            if (mBufLen > 0 && mDataPath != null) {
                byte[] lastData = Arrays.copyOf(mBuf, mBufLen);
                Logger.d("VoiceRecorder release lastData.length = " + lastData.length);
                mDataPath.onData(lastData);
            }
            mVoiceSource.stop();
        }

        private void copy(byte[] audioData, int readSize) {
            int tempLen = readSize;
            int tempIdx = 0;
            int length;
            while (tempLen + mBufLen >= S_BYTE_LEN) {
                length = S_BYTE_LEN - mBufLen;
                System.arraycopy(audioData, tempIdx, mBuf, mBufLen, length);
                tempLen -= length;
                tempIdx += length;
                mDataPath.onData(mBuf);
                mBufLen = 0;
            }
            System.arraycopy(audioData, tempIdx, mBuf, mBufLen, tempLen);
            mBufLen += tempLen;
        }
    }
}
