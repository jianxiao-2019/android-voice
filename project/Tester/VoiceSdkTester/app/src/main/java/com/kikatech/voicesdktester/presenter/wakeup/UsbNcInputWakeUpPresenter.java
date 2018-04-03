package com.kikatech.voicesdktester.presenter.wakeup;

import android.content.Context;

/**
 * Created by ryanlin on 02/04/2018.
 */

public class UsbNcInputWakeUpPresenter extends UsbInputWakeUpPresenter {

    public UsbNcInputWakeUpPresenter(Context context) {
        super(context);
    }

    @Override
    protected boolean isUsingNc() {
        return true;
    }
}
