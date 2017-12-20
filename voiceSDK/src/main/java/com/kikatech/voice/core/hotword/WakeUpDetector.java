package com.kikatech.voice.core.hotword;

import android.content.Context;

import com.kikatech.voice.core.framework.IDataPath;
import com.kikatech.voice.util.log.Logger;

import ai.kitt.snowboy.AppResCopy;

/**
 * Created by ryanlin on 06/12/2017.
 * Update by ryanlin on 25/12/2017.
 */

public abstract class WakeUpDetector {

    protected OnHotWordDetectListener mListener;
    private IDataPath mDataPath;

    WakeUpDetector(OnHotWordDetectListener listener) {
        mListener = listener;
    }

    public static WakeUpDetector getDetector(Context context, OnHotWordDetectListener listener) {
        // TODO : SnowBoyDetector is the only one implement of WakeUpDetector.
        AppResCopy.copyResFromAssetsToSD(context);
        return new SnowBoyDetector(listener);
    }

    public final void setNextDataPath(IDataPath nextPath) {
        Logger.d("WakeUpDetector setNextDataPath next path = " + nextPath);
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
        public final void onData(byte[] data) {
            Logger.d("WakeUpDataPath onData data.length = " + data.length + " isAwake = " + isAwake());
            if (isAwake()) {
                if (mNextPath != null) {
                    mNextPath.onData(data);
                }
            } else {
                checkWakeUpCommand(data);
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
}