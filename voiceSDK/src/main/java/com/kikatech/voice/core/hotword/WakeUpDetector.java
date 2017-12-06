package com.kikatech.voice.core.hotword;

import com.kikatech.voice.core.framework.IDataPath;
import com.kikatech.voice.util.log.Logger;

/**
 * Created by ryanlin on 06/12/2017.
 */

public abstract class WakeUpDetector implements IDataPath {

    private IDataPath mDataPath;

    public WakeUpDetector(IDataPath iDataPath) {
        mDataPath = iDataPath;
    }

    public interface OnHotWordDetectListener {
        void onDetected();
    }

    public static WakeUpDetector getDetector(OnHotWordDetectListener listener, IDataPath dataPath) {
        return new SnowBoyDetector(listener, dataPath);
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
    protected abstract boolean isAwake();
    public abstract void goSleep();
}
