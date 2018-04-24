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
                    Logger.d(this + " [open] mVoiceSource = " + mVoiceSource);
                    boolean success = mVoiceSource.open();
                    if (!success) {
                        Logger.e("Voice source open fail!");
                        if (mListener != null) {
                            mListener.onRecorderError(ERR_OPEN_FAIL);
                        }
                    }
                }
            }
        });
    }

    public void start() {
        Logger.v(this + " start mAudioRecordThread = " + mAudioRecordThread
                + " Thread = " + Thread.currentThread().getName());
        if (mAudioRecordThread != null) {
            mAudioRecordThread.stop();
        }
        mAudioRecordThread = new AudioRecordThread();
        EXECUTOR.execute(mAudioRecordThread);
    }

    public void stop() {
        Logger.v(this + " stop mAudioRecordThread = " + mAudioRecordThread
                + " Thread = " + Thread.currentThread().getName());
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
                Logger.d(this + " [close] mVoiceSource = " + mVoiceSource);
                if (mVoiceSource != null) {
                    mVoiceSource.close();
                }
            }
        });
    }

    private class AudioRecordThread implements Runnable {

        private static final int FAIL_COUNT_THRESHOLD = 10;
        private static final int S_BYTE_LEN = (int) (4096 * 1.5);//vad中的输入是一段4096字节的音频

        private boolean mIsRunning = true;

        private byte[] mBuf = new byte[S_BYTE_LEN];
        private int mBufLen = 0;

        private int mFailCount = 0;

        public void stop() {
            mIsRunning = false;
        }

        @Override
        public void run() {
            Logger.d(this + " [prepare]");
            mVoiceSource.start();

            Logger.d(this + " [record] bufferSize = " + mVoiceSource.getBufferSize());
            byte[] audioData = new byte[mVoiceSource.getBufferSize()];
            int readSize;
            while (mIsRunning) {
                readSize = mVoiceSource.read(audioData, 0, mVoiceSource.getBufferSize());

                if (mVoiceDataListener != null) {
                    mVoiceDataListener.onData(audioData, readSize);
                }

//                if (AudioRecord.ERROR_INVALID_OPERATION != readSize /*&& fos != null*/) {
                if (readSize > 0) {
                    copy(audioData, readSize);
                    mFailCount = 0;
                } else {
                    Logger.e("[AudioRecordThread] readSize = " + readSize + " " + mVoiceSource);
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if (++mFailCount == FAIL_COUNT_THRESHOLD) {
                        Logger.e("[AudioRecordThread] FAIL_COUNT_THRESHOLD");
                        if (mListener != null) {
                            mListener.onRecorderError(ERR_RECORD_FAIL);
                        }
                        break;
                    }
                }
            }

            Logger.d(this + " [release]");
            if (mBufLen > 0 && mDataPath != null) {
                byte[] lastData = Arrays.copyOf(mBuf, mBufLen);
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
