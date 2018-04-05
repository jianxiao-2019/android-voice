package com.kikatech.voicesdktester.presenter.wakeup;

import android.content.Context;

import com.kikatech.voicesdktester.source.LocalNcVoiceSource;
import com.kikatech.voicesdktester.source.LocalVoiceSource;

/**
 * Created by ryanlin on 03/04/2018.
 */

public class LocalWakeUpNcPresenter extends LocalWakeUpPresenter {

    public LocalWakeUpNcPresenter(Context context) {
        super(context);
    }

    @Override
    protected LocalVoiceSource getLocalVoiceSource() {
        return new LocalNcVoiceSource();
    }
}
