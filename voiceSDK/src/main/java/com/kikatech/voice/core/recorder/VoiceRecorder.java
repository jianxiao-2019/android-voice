package com.kikatech.voice.core.recorder;

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

    private final IDataPath mDataPath;
    private final IVoiceSource mVoiceSource;

    private ExecutorService mExecutor = Executors.newSingleThreadExecutor();
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
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                if (mVoiceSource != null) {
                    mVoiceSource.open();
                }
            }
        });
    }

    public void start() {
        Logger.d("[VoiceRecorder] start mAudioRecordThread = " + mAudioRecordThread);
        if (mAudioRecordThread != null) {
            mAudioRecordThread.stop();
        }
        mAudioRecordThread = new AudioRecordThread();
        mExecutor.execute(mAudioRecordThread);
    }

    public void stop() {
        Logger.d("[VoiceRecorder] stopRecording mAudioRecordThread = " + mAudioRecordThread);
        if (mAudioRecordThread != null) {
            mAudioRecordThread.stop();
            mAudioRecordThread = null;
        }
    }

    public void close() {
        if (mAudioRecordThread != null) {
            Logger.e("Please call stop() first.");
        }
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                if (mVoiceSource != null) {
                    mVoiceSource.close();
                }
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
            Logger.d("[VoiceRecorder] AudioRecordThread stop");
            mIsRunning.set(false);
        }

        @Override
        public void run() {
            Logger.i("[VoiceRecorder] AudioRecordThread prepare");
            mIsRunning.set(true);
            mVoiceSource.start();

            Logger.i("[VoiceRecorder] AudioRecordThread record bufferSize = " + mVoiceSource.getBufferSize());
            byte[] audioData = new byte[mVoiceSource.getBufferSize()];
            int readSize;
            while (mIsRunning.get()) {
                readSize = mVoiceSource.read(audioData, 0, mVoiceSource.getBufferSize());

                if (mVoiceDataListener != null) {
                    mVoiceDataListener.onData(audioData, readSize);
                }

//                if (AudioRecord.ERROR_INVALID_OPERATION != readSize /*&& fos != null*/) {
                if (readSize > 0) {
                    copy(audioData, readSize);
                    mFailCount = 0;
                } else {
                    Logger.e("[VoiceRecorder][AudioRecordThread][Err] readSize = " + readSize + " mVoiceSource:" + mVoiceSource);
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if (mListener != null && ++mFailCount == FAIL_COUNT_THRESHOLD) {
                        Logger.e("[AudioRecordThread][Err] FAIL_COUNT_THRESHOLD");
                        mListener.onRecorderError(0);
                        break;
                    }
                }
            }

            Logger.i("[VoiceRecorder] AudioRecordThread release");
            if (mBufLen > 0 && mDataPath != null) {
                byte[] lastData = Arrays.copyOf(mBuf, mBufLen);
                Logger.d("[VoiceRecorder] release lastData.length = " + lastData.length);
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
