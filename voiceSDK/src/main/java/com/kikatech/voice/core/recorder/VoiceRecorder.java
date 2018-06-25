package com.kikatech.voice.core.recorder;

import com.kikatech.voice.core.framework.IDataPath;
import com.kikatech.voice.core.recorder.executor.AudioRecordExecutor;
import com.kikatech.voice.util.log.Logger;

/**
 * @author SkeeterWang Created on 2018/6/4.
 */

public class VoiceRecorder {
    private static final String TAG = "VoiceRecorderN";

    public static final int ERR_OPEN_FAIL = 1;
    public static final int ERR_RECORD_FAIL = 2;

    private final AudioRecordExecutor mAudioRecordExecutor = new AudioRecordExecutor();

    private final IDataPath mDataPath;
    private final IVoiceSource mVoiceSource;
    private AudioRecordTask mTask;

    private IRecorderListener mListener;

    public interface IRecorderListener {
        void onRecorderData(byte[] data, int length);

        void onRecorderError(int errorCode);
    }

    public VoiceRecorder(IVoiceSource voiceSource, IDataPath dataPath, IRecorderListener listener) {
        mVoiceSource = voiceSource;
        mDataPath = dataPath;
        mListener = listener;
    }

    @SuppressWarnings("FieldCanBeLocal")
    private final AudioRecordTask.IRecordingListener mRecordingListener = new AudioRecordTask.IRecordingListener() {
        @Override
        public void onData(byte[] data, int length) {
            dispatchData(data, length);
        }

        @Override
        public void onError(int error) {
            switch (error) {
                case AudioRecordTask.RecordingError.OPEN_FAILED:
                    dispatchError(ERR_OPEN_FAIL);
                    break;
                case AudioRecordTask.RecordingError.RECORD_FAILED:
                    dispatchError(ERR_RECORD_FAIL);
                    break;
            }
        }

        private synchronized void dispatchData(byte[] data, int length) {
            if (mListener != null) {
                mListener.onRecorderData(data, length);
            }
        }

        private synchronized void dispatchError(int error) {
            if (mListener != null) {
                mListener.onRecorderError(error);
            }
        }
    };

    public void open() {
        if (Logger.DEBUG) {
            Logger.i(TAG, "open");
        }
        mAudioRecordExecutor.execute(new Runnable() {
            @Override
            public void run() {
                android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);
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
        if (Logger.DEBUG) {
            Logger.i(TAG, String.format("start, mTask: %s", mTask));
        }
        if (mTask != null) {
            mAudioRecordExecutor.remove(mTask);
        }
        mTask = new AudioRecordTask(mVoiceSource, mDataPath, mRecordingListener);
        mAudioRecordExecutor.execute(mTask);
    }

    public void stop() {
        if (Logger.DEBUG) {
            Logger.i(TAG, String.format("stop, mTask: %s", mTask));
        }
        if (mTask != null) {
            mAudioRecordExecutor.remove(mTask);
            mTask = null;
        }
    }

    public void close() {
        if (Logger.DEBUG) {
            Logger.i(TAG, "close");
        }
        stop();
        mAudioRecordExecutor.cleanAll();
        mAudioRecordExecutor.execute(new Runnable() {
            @Override
            public void run() {
                if (mVoiceSource != null) {
                    mVoiceSource.close();
                }
            }
        });
    }
}