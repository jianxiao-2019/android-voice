package com.kikatech.voicesdktester.presenter.wakeup;

import android.content.Context;

import com.kikatech.voice.core.debug.DebugUtil;

/**
 * Created by ryanlin on 02/04/2018.
 */

public class AndroidWakeUpPresenter extends WakeUpPresenter {

    public AndroidWakeUpPresenter(Context context) {
        super(context);
    }

    @Override
    public void prepare() {
        attachService();
    }

    @Override
    public void onWakeUp() {
        String path = DebugUtil.getDebugFilePath();
        super.onWakeUp();

        renameSuccessFile(path, "_SRC");
        renameSuccessFile(path, "_COMMAND");
    }

    @Override
    protected void attachService() {
        super.attachService();
        if (mCallback != null) {
            mCallback.onUpdateStatus("Using Android");
        }
    }
}
