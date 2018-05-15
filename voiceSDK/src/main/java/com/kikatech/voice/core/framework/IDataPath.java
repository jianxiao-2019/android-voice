package com.kikatech.voice.core.framework;

import com.kikatech.voice.util.log.Logger;

/**
 * Created by tianli on 17-10-28.
 * Update by ryanlin on 25/12/2017.
 */

public abstract class IDataPath {

    protected final IDataPath mNextPath;

    public IDataPath(IDataPath nextPath) {
        mNextPath = nextPath;
    }

    public void start() {
        if (mNextPath != null) {
            mNextPath.start();
        }
    }

    public void stop() {
        if (mNextPath != null) {
            mNextPath.stop();
        }
    }

    public void dump() {
        Logger.d("IDataPath dump this = " + this);
        if (mNextPath != null) {
            mNextPath.dump();
        }
    }

    public abstract void onData(byte[] data, int length);

    public String toString() {
        return "[" + getClass().getSimpleName() + "]";
    }
}
