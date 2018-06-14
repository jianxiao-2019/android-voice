package com.kikatech.voice.core.hotword;

import com.kikatech.voice.core.debug.FileWriter;
import com.kikatech.voice.core.framework.IDataPath;
import com.kikatech.voice.util.log.Logger;

/**
 * Created by ryanlin on 06/12/2017.
 * Update by ryanlin on 25/12/2017.
 */

public abstract class WakeUpDetector {

    protected OnHotWordDetectListener mListener;
    private IDataPath mDataPath;

    private FileWriter mFileWriter;

    public WakeUpDetector() {
        // mFileWriter = new FileWriter("_COMMAND", null);
        mFileWriter = null;
    }

    public void setOnHotWordDetectListener(OnHotWordDetectListener listener) {
        mListener = listener;
    }

    public final void setNextDataPath(IDataPath nextPath) {
        mDataPath = new WakeUpDataPath(nextPath);
    }

    public final IDataPath getDataPath() {
        return mDataPath;
    }

    private class WakeUpDataPath extends IDataPath {

        WakeUpDataPath(IDataPath nextPath) {
            super(nextPath);
        }

        @Override
        public void start() {
            if (isAwake()) {
                super.start();
            }
            if (mFileWriter != null) {
                mFileWriter.start();
            }
        }

        @Override
        public final void onData(byte[] data, int length) {
            if (isAwake()) {
                if (mNextPath != null) {
                    mNextPath.onData(data, length);
                }
            } else {
                Logger.v("onData data.length = " + length);
                checkWakeUpCommand(data);
                if (mFileWriter != null) {
                    mFileWriter.onData(data, length);
                }
            }
        }
    }

    public interface OnHotWordDetectListener {
        void onDetected();
    }

    protected abstract void checkWakeUpCommand(byte[] data);

    public abstract boolean isAwake();

    public abstract void reset();

    public abstract void goSleep();

    public abstract void wakeUp();

    public abstract void close();

    public abstract void enableDetector(boolean enable);

    public abstract boolean isEnabled();
}