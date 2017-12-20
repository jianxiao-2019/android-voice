package com.kikatech.voice.core.hotword;

import com.kikatech.voice.core.framework.IDataPath;
import com.kikatech.voice.util.log.Logger;

/**
 * Created by ryanlin on 06/12/2017.
 */

public abstract class WakeUpDetector implements IDataPath {

    private IDataPath mDataPath;
    protected boolean mStereoVoice;

    public WakeUpDetector(IDataPath iDataPath, boolean stereoVoice) {
        mDataPath = iDataPath;
        mStereoVoice = stereoVoice;
    }

    public interface OnHotWordDetectListener {
        void onDetected();
    }

    public static WakeUpDetector getDetector(OnHotWordDetectListener listener, IDataPath dataPath, String dbgPath, boolean stereoVoice) {
        return new SnowBoyDetector(listener, dataPath, dbgPath, stereoVoice);
    }

    @Override
    public final void onData(byte[] data) {
        Logger.d("WakeUpDetector onData data.length = " + data.length + " isAwake = " + isAwake());
        if (isAwake()) {
            if (mDataPath != null) {
                mDataPath.onData(data);
            }
        } else {
            checkWakeUpCommand(data);
        }
    }

    protected abstract void checkWakeUpCommand(byte[] data);
    public abstract boolean isAwake();
    public abstract void reset();
    public abstract void goSleep();
    public abstract void wakeUp();
    public abstract void close();
}
