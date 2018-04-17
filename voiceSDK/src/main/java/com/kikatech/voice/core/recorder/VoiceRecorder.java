package com.kikatech.voice.core.recorder;

import android.os.Process;

import com.kikatech.voice.core.framework.IDataPath;
import com.kikatech.voice.service.voice.VoiceService;
import com.kikatech.voice.util.log.Logger;

import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by tianli on 17-10-28.
 * Update by ryanlin on 25/12/2017.
 */

public class VoiceRecorder {

    public static final int ERR_OPEN_FAIL = 1;
    public static final int ERR_RECORD_FAIL = 2;

    private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor();

    private final IDataPath mDataPath;
    private final IVoiceSource mVoiceSource;

    private AudioRecordThread mAudioRecordThread;

    private VoiceService.VoiceDataListener mVoiceDataListener;
    private OnRecorderErrorListener mListener;

    public interface OnRecorderErrorListener {
        void onRecorderError(int errorCode);
    }

    public VoiceRecorder(IVoiceSource voiceSource, IDataPath dataPath,
                         OnRecorderErrorListener listener) {
        mVoiceSource = voiceSource;
        mDataPath = dataPath;
        mListener = listener;
    }

    public void setVoiceDataListener(VoiceService.VoiceDataListener listener) {
        mVoiceDataListener = listener;
    }

    public void open() {
        EXECUTOR.execute(new Runnable() {
            @Override
            public void run() {
                android.os.Process.setThreadPriority(Process.THREAD_PRIORITY_URGENT_AUDIO);
                if (mVoiceSource != null) {
                    Logger.i(this + " [open] mVoiceSource = " + mVoiceSource + " Thread = " + Thread.currentThread().getName());
                    boolean success = mVoiceSource.open();
                    if (!success && mListener != null) {
                        mListener.onRecorderError(ERR_OPEN_FAIL);
                    }
                }

                Logger.i(this + " [open] enddddddd");
            }
        });
    }

    public void start() {
        Logger.d(this + " start mAudioRecordThread = " + mAudioRecordThread + " Thread = " + Thread.currentThread().getName());
        Logger.d(this + " start mExecutor isShutDown = " + EXECUTOR.isShutdown() + " isTerminated = " + EXECUTOR.isTerminated());
        if (mAudioRecordThread != null) {
            mAudioRecordThread.stop();
        }
        mAudioRecordThread = new AudioRecordThread();
        EXECUTOR.execute(mAudioRecordThread);
    }

    public void stop() {
        Logger.d(this + " stopRecording mAudioRecordThread = " + mAudioRecordThread + " Thread = " + Thread.currentThread().getName());
        if (mAudioRecordThread != null) {
            mAudioRecordThread.stop();
            mAudioRecordThread = null;
        }
    }

    public void close() {
        if (mAudioRecordThread != null) {
            Logger.e("Please call stop() first.");
        }
        EXECUTOR.execute(new Runnable() {
            @Override
            public void run() {
                Logger.i(this + " [close] mVoiceSource = " + mVoiceSource + " Thread = " + Thread.currentThread().getName());
                if (mVoiceSource != null) {
                    mVoiceSource.close();
                }

                Logger.i(this + " [close] enddddddd");
            }
        });
    }

    private class AudioRecordThread implements Runnable {

        private static final int FAIL_COUNT_THRESHOLD = 10;
        private static final int S_BYTE_LEN = (int) (4096 * 1.5);//vad中的输入是一段4096字节的音频

        private AtomicBoolean mIsRunning = new AtomicBoolean(false);

        private byte[] mBuf = new byte[S_BYTE_LEN];
        private int mBufLen = 0;

        private int mFailCount = 0;

        public void stop() {
            Logger.d(this + " stop Thread = " + Thread.currentThread().getName());
            mIsRunning.set(false);
        }

        @Override
        public void run() {
            Logger.i(this + " [prepare] Thread = " + Thread.currentThread().getName());
            mIsRunning.set(true);
            mVoiceSource.start();

            Logger.i(this + " [record] bufferSize = " + mVoiceSource.getBufferSize() + " Thread = " + Thread.currentThread().getName());
            byte[] audioData = new byte[mVoiceSource.getBufferSize()];
            int readSize;
            while (mIsRunning.get()) {
                Logger.v("voiceRecorder before read");
                readSize = mVoiceSource.read(audioData, 0, mVoiceSource.getBufferSize());
                Logger.v("voiceRecorder read new size = " + readSize);

                if (mVoiceDataListener != null) {
                    mVoiceDataListener.onData(audioData, readSize);
                }

//                if (AudioRecord.ERROR_INVALID_OPERATION != readSize /*&& fos != null*/) {
                if (readSize > 0) {
                    copy(audioData, readSize);
                    mFailCount = 0;
                } else {
                    Logger.e("AudioRecordThread[Err] readSize = " + readSize + " mVoiceSource:" + mVoiceSource);
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if (mListener != null && ++mFailCount == FAIL_COUNT_THRESHOLD) {
                        Logger.e("AudioRecordThread[Err] FAIL_COUNT_THRESHOLD");
                        mListener.onRecorderError(ERR_RECORD_FAIL);
                        break;
                    }
                }
            }

            Logger.i(this + " [release] Thread = " + Thread.currentThread().getName());
            if (mBufLen > 0 && mDataPath != null) {
                byte[] lastData = Arrays.copyOf(mBuf, mBufLen);
                Logger.d("release lastData.length = " + lastData.length);
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
