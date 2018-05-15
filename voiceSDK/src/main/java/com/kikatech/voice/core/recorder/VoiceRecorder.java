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
        Logger.d("open");
        EXECUTOR.execute(new Runnable() {
            @Override
            public void run() {
                android.os.Process.setThreadPriority(Process.THREAD_PRIORITY_URGENT_AUDIO);
                if (mVoiceSource != null) {
                    Logger.v(VoiceRecorder.this + " [open] mVoiceSource = " + mVoiceSource);
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
        Logger.v(this + " start mAudioRecordThread = " + mAudioRecordThread);
        if (mAudioRecordThread != null) {
            mAudioRecordThread.stop();
        }
        mAudioRecordThread = new AudioRecordThread();
        EXECUTOR.execute(mAudioRecordThread);
    }

    public void stop() {
        Logger.v(this + " stop mAudioRecordThread = " + mAudioRecordThread);
        if (mAudioRecordThread != null) {
            mAudioRecordThread.stop();
            mAudioRecordThread = null;
        }
    }

    public void close() {
        Logger.d("close");
        if (mAudioRecordThread != null) {
            Logger.e("Please call stop() first.");
        }
        EXECUTOR.execute(new Runnable() {
            @Override
            public void run() {
                Logger.v(VoiceRecorder.this + " [close] mVoiceSource = " + mVoiceSource);
                if (mVoiceSource != null) {
                    mVoiceSource.close();
                }
            }
        });
    }

    private class AudioRecordThread implements Runnable {

        private static final int FAIL_COUNT_THRESHOLD = 10;

        private int mFailCount = 0;

        private boolean mIsRunning = true;

        private ExecutorService mExecutor = Executors.newSingleThreadExecutor();

        public void stop() {
            mIsRunning = false;
        }

        @Override
        public void run() {
            Logger.v(VoiceRecorder.this + " [prepare]");
            android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_AUDIO);
            
            mVoiceSource.start();

            Logger.v(VoiceRecorder.this + " [record] bufferSize = " + mVoiceSource.getBufferSize());

            while (mIsRunning) {
                final byte[] audioData = new byte[mVoiceSource.getBufferSize()];
                final int readSize = mVoiceSource.read(audioData, 0, mVoiceSource.getBufferSize());

                if (mVoiceDataListener != null) {
                    mVoiceDataListener.onData(audioData, readSize);
                }

                if (readSize > 0) {
                    mExecutor.execute(new Runnable() {
                        @Override
                        public void run() {
                            mDataPath.onData(audioData, readSize);
                        }
                    });
                    mFailCount = 0;
                } else {
                    Logger.e("[AudioRecordThread] readSize = " + readSize + " " + mVoiceSource);

                    if (++mFailCount == FAIL_COUNT_THRESHOLD) {
                        Logger.e("[AudioRecordThread] FAIL_COUNT_THRESHOLD");
                        if (mListener != null) {
                            mListener.onRecorderError(ERR_RECORD_FAIL);
                        }
                        break;
                    }

                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }

            Logger.v(VoiceRecorder.this + " [release]");

            mVoiceSource.stop();
        }

    }
}
