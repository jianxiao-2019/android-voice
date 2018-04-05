package com.kikatech.voicesdktester.presenter.wakeup;

import android.content.Context;

import com.kikatech.voicesdktester.source.LocalMonoVoiceSource;
import com.kikatech.voicesdktester.source.LocalVoiceSource;

/**
 * Created by ryanlin on 03/04/2018.
 */

public class LocalWakeUpMonoPresenter extends LocalWakeUpPresenter {

    public LocalWakeUpMonoPresenter(Context context) {
        super(context);
    }

    @Override
    protected LocalVoiceSource getLocalVoiceSource() {
        return new LocalMonoVoiceSource();
    }
}
