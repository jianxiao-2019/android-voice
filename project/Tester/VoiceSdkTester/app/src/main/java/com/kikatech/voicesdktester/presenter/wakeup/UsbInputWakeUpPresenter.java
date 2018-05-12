package com.kikatech.voicesdktester.presenter.wakeup;

import android.content.Context;

import com.kikatech.usb.IUsbAudioListener;
import com.kikatech.usb.UsbAudioService;
import com.kikatech.usb.buffer.KikaBuffer;
import com.kikatech.usb.datasource.KikaGoVoiceSource;
import com.kikatech.voice.core.debug.DebugUtil;
import com.kikatech.voice.util.log.Logger;

/**
 * Created by ryanlin on 02/04/2018.
 */

public abstract class UsbInputWakeUpPresenter extends WakeUpPresenter {

    private KikaGoVoiceSource mKikaGoVoiceSource;
    private UsbAudioService mUsbAudioService;

    public UsbInputWakeUpPresenter(Context context) {
        super(context);
    }

    protected abstract boolean isUsingNc();

    @Override
    public void prepare() {
        mUsbAudioService = UsbAudioService.getInstance(mContext);
        mUsbAudioService.setListener(mIUsbAudioListener);
        mUsbAudioService.scanDevices();
    }

    @Override
    public void setFilePath(String filePath) {
    }

    private IUsbAudioListener mIUsbAudioListener = new IUsbAudioListener() {

        @Override
        public void onDeviceAttached(KikaGoVoiceSource audioSource) {
            Logger.d("onDeviceAttached. mIsUsingNc = " + isUsingNc());
            mKikaGoVoiceSource = audioSource;
            mKikaGoVoiceSource.setKikaBuffer(isUsingNc() ? KikaBuffer.TYPE_NOISC_CANCELLATION : KikaBuffer.TYPE_STEREO_TO_MONO);

            mVoiceSource = mKikaGoVoiceSource;
            attachService();
        }

        @Override
        public void onDeviceDetached() {
            Logger.d("onDeviceDetached.");
            mKikaGoVoiceSource = null;
            mVoiceSource = null;
            attachService();
        }

        @Override
        public void onDeviceError(int errorCode) {
            mKikaGoVoiceSource = null;
            mVoiceSource = null;
            attachService();
        }
    };

    @Override
    protected void attachService() {
        super.attachService();

        if (mCallback != null) {
            mCallback.onUpdateStatus(!isUsingNc() ? "Using KikaGO No NC" : "Using KikaGO with NC");
        }
    }

    @Override
    public void onWakeUp() {
        String path = DebugUtil.getDebugFilePath();
        super.onWakeUp();

        renameSuccessFile(path, "_USB");
        renameSuccessFile(path, "_COMMAND");
    }

    @Override
    public void close() {
        super.close();
        if (mUsbAudioService != null) {
            mUsbAudioService.closeDevice();
            mUsbAudioService.setListener(null);
        }
    }
}

