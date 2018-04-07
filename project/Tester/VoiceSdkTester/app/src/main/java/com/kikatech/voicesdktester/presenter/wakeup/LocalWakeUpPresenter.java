package com.kikatech.voicesdktester.presenter.wakeup;

import android.content.Context;
import android.os.Handler;
import android.text.TextUtils;

import com.kikatech.voice.service.voice.VoiceService;
import com.kikatech.voice.util.log.Logger;
import com.kikatech.voicesdktester.source.LocalVoiceSource;

/**
 * Created by ryanlin on 03/04/2018.
 */

public abstract class LocalWakeUpPresenter extends WakeUpPresenter implements LocalVoiceSource.EofListener {

    private LocalVoiceSource mLocalVoiceSource;
    private final Handler mUiHandler;

    public LocalWakeUpPresenter(Context context) {
        super(context);

        mUiHandler = new Handler();
    }

    @Override
    public void prepare() {
        mLocalVoiceSource = getLocalVoiceSource();
        Logger.i("r5r5 LocalWakeUpPresenter mLocalVoiceSource = " + mLocalVoiceSource);
        mLocalVoiceSource.setEofListener(this);

        mVoiceSource = mLocalVoiceSource;
        if (mCallback != null) {
            mCallback.onReadyStateChanged(false);
        }
        attachService();
    }

    @Override
    public void setFilePath(String filePath) {
        Logger.d("LocalWakeUpPresenter setFilePath filePath = " + filePath);
        if (TextUtils.isEmpty(filePath)) {
            if (mCallback != null) {
                mCallback.onReadyStateChanged(false);
            }
            return;
        }

        if (mLocalVoiceSource != null) {
            mLocalVoiceSource.setTargetFile(filePath);
        }
        if (mCallback != null) {
            mCallback.onReadyStateChanged(true);
        }
    }

    @Override
    public void onEndOfFile() {
        Logger.d("LocalWakeUpPresenter onEndOfFile");
        mUiHandler.postDelayed(() -> mVoiceService.stop(VoiceService.StopType.NORMAL), 500);
    }

    protected abstract LocalVoiceSource getLocalVoiceSource();
}
