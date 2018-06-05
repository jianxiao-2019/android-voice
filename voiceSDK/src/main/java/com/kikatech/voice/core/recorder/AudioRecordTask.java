package com.kikatech.voice.core.recorder;

import android.support.annotation.IntDef;

import com.kikatech.voice.core.framework.IDataPath;
import com.kikatech.voice.core.recorder.executor.DataPathExecutor;
import com.kikatech.voice.core.recorder.executor.IRunnable;
import com.kikatech.voice.util.log.Logger;

/**
 * @author SkeeterWang Created on 2018/6/4.
 */

public class AudioRecordTask implements IRunnable {
    private static final String TAG = "AudioRecordTask";

    private static final int ERR_OPEN_FAILED = 1;
    private static final int ERR_RECORD_FAILED = 2;

    @IntDef({ERR_OPEN_FAILED, ERR_RECORD_FAILED})
    public @interface RecordingError {
        int OPEN_FAILED = ERR_OPEN_FAILED;
        int RECORD_FAILED = ERR_RECORD_FAILED;
    }

    private static final int FAIL_COUNT_THRESHOLD = 10;

    private static final int STATE_PREPARED = 0;
    private static final int STATE_RUNNING = 1;
    private static final int STATE_INTERRUPT = 2;
    private static final int STATE_STOPPED = 3;

    @IntDef({STATE_PREPARED, STATE_RUNNING, STATE_INTERRUPT})
    private @interface RecordingState {
        int PREPARED = STATE_PREPARED;
        int RUNNING = STATE_RUNNING;
        int INTERRUPT = STATE_INTERRUPT;
        int STOPPED = STATE_STOPPED;
    }


    private IVoiceSource mVoiceSource;
    private IDataPath mDataPath;
    private IRecordingListener mListener;

    private final DataPathExecutor mDataPathExecutor = new DataPathExecutor();

    @RecordingState
    private int mStatus;

    private int mFailCount = 0;

    AudioRecordTask(IVoiceSource voiceSource, IDataPath dataPath, IRecordingListener listener) {
        mVoiceSource = voiceSource;
        mDataPath = dataPath;
        mListener = listener;
    }

    @Override
    public synchronized void cancel() {
        mStatus = RecordingState.INTERRUPT;
    }

    @Override
    public synchronized boolean isRunning() {
        return mStatus == RecordingState.RUNNING;
    }

    @Override
    public void run() {
        if (mVoiceSource == null || mDataPath == null) {
            if (Logger.DEBUG) {
                Logger.w(TAG, "invalid VoiceSource or DataPath ... stop.");
            }
            return;
        }

        if (mStatus != RecordingState.PREPARED) {
            if (Logger.DEBUG) {
                Logger.w(TAG, "invalid running status ... stop.");
            }
            return;
        }

        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_AUDIO);

        onStart();

        final int BUFFER_SIZE = mVoiceSource.getBufferSize();
        while (isRunning()) {
            final byte[] audioData = new byte[BUFFER_SIZE];
            final int readSize = mVoiceSource.read(audioData, 0, BUFFER_SIZE);

            if (readSize > 0) {
                mDataPathExecutor.execute(new Runnable() {
                    @Override
                    public void run() {
                        mDataPath.onData(audioData, readSize);
                    }
                });
                mFailCount = 0;
            } else {
                if (Logger.DEBUG) {
                    Logger.e(TAG, String.format("[AudioRecordThread] readSize = %s, %s", readSize, mVoiceSource));
                }
                if (++mFailCount >= FAIL_COUNT_THRESHOLD) {
                    if (Logger.DEBUG) {
                        Logger.e(TAG, "[AudioRecordThread] FAIL_COUNT_THRESHOLD");
                    }
                    if (mListener != null) {
                        mListener.onError(RecordingError.RECORD_FAILED);
                    }
                    break;
                }
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    if (Logger.DEBUG) {
                        Logger.printStackTrace(TAG, e.getMessage(), e);
                    }
                }
            }
        }

        onStop();
    }

    private synchronized void onStart() {
        mStatus = RecordingState.RUNNING;
        if (mVoiceSource != null) {
            mVoiceSource.start();
        }
        if (mDataPath != null) {
            mDataPathExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    mDataPath.start();
                }
            });
        }
    }

    private synchronized void onStop() {
        mStatus = RecordingState.STOPPED;
        if (mVoiceSource != null) {
            mVoiceSource.stop();
        }
        mDataPathExecutor.cleanAll();
        if (mDataPath != null) {
            mDataPathExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    mDataPath.stop();
                }
            });
        }
    }

    public interface IRecordingListener {
        void onError(@RecordingError int error);
    }
}
