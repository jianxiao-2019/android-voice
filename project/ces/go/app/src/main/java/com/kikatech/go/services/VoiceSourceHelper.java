package com.kikatech.go.services;

import android.content.Context;
import android.support.annotation.IntDef;

import com.kikatech.go.util.BackgroundThread;
import com.kikatech.go.util.LogUtil;
import com.kikatech.usb.IUsbAudioListener;
import com.kikatech.usb.UsbAudioService;
import com.kikatech.usb.UsbAudioSource;

/**
 * @author SkeeterWang Created on 2018/3/13.
 */

public class VoiceSourceHelper {
    private static final String TAG = "VoiceSourceHelper";

    private static final int USB_SCAN_TIME_OUT_MS = 800;

    private static final int CHANGED_REASON_USB_ATTACHED = 1;
    private static final int CHANGED_REASON_USB_DETACHED = 2;
    private static final int CHANGED_REASON_USB_DEVICE_NOT_FOUND = 3;
    private static final int CHANGED_REASON_USB_DEVICE_ERROR = 4;
    private static final int CHANGED_REASON_SCAN_TIMEOUT = 5;

    @IntDef({CHANGED_REASON_USB_ATTACHED, CHANGED_REASON_USB_DETACHED, CHANGED_REASON_USB_DEVICE_NOT_FOUND, CHANGED_REASON_USB_DEVICE_ERROR, CHANGED_REASON_SCAN_TIMEOUT})
    public @interface ChangedReason {
        int USB_ATTACHED = CHANGED_REASON_USB_ATTACHED;
        int USB_DETACHED = CHANGED_REASON_USB_DETACHED;
        int USB_DEVICE_NOT_FOUND = CHANGED_REASON_USB_DEVICE_NOT_FOUND;
        int USB_DEVICE_ERROR = CHANGED_REASON_USB_DEVICE_ERROR;
        int SCAN_TIMEOUT = CHANGED_REASON_SCAN_TIMEOUT;
    }

    private long start_t;

    private IVoiceSourceListener mVoiceSourceListener;
    private UsbAudioSource mUsbVoiceSource;
    private IUsbAudioListener mUsbListener = new IUsbAudioListener() {
        @Override
        public void onDeviceAttached(UsbAudioSource audioSource) {
            if (LogUtil.DEBUG) {
                LogUtil.logv(TAG, String.format("onDeviceAttached, spend: %s", (System.currentTimeMillis() - start_t)));
            }
            stopScanTimer();
            if (mUsbVoiceSource == null) {
                mUsbVoiceSource = audioSource;
                if (mVoiceSourceListener != null) {
                    mVoiceSourceListener.onVoiceSourceChanged(mUsbVoiceSource, ChangedReason.USB_ATTACHED);
                }
            }
        }

        @Override
        public void onDeviceDetached() {
            if (LogUtil.DEBUG) {
                LogUtil.logv(TAG, String.format("onDeviceDetached, spend: %s", (System.currentTimeMillis() - start_t)));
            }
            stopScanTimer();
            if (mVoiceSourceListener != null && mUsbVoiceSource != null) {
                closeUsbVoiceSource();
                mVoiceSourceListener.onVoiceSourceChanged(null, ChangedReason.USB_DETACHED);
            }
        }

        @Override
        public void onDeviceError(int errorCode) {
            if (LogUtil.DEBUG) {
                LogUtil.logw(TAG, String.format("onDeviceError, errorCode: %s", errorCode));
            }
            stopScanTimer();

            switch (errorCode) {
                case IUsbAudioListener.ERROR_NO_DEVICES:
                    if (mVoiceSourceListener != null) {
                        mVoiceSourceListener.onVoiceSourceChanged(null, ChangedReason.USB_DEVICE_NOT_FOUND);
                    }
                    break;
                default:
                    if (mVoiceSourceListener != null && mUsbVoiceSource != null) {
                        closeUsbVoiceSource();
                        mVoiceSourceListener.onVoiceSourceChanged(null, ChangedReason.USB_DEVICE_ERROR);
                    }
                    break;
            }
        }
    };
    private Runnable mUsbScanTimeoutTask = new Runnable() {
        @Override
        public void run() {
            onTimeout();
        }

        private void onTimeout() {
            if (LogUtil.DEBUG) {
                LogUtil.logw(TAG, String.format("onTimeout, spend: %s ms", (System.currentTimeMillis() - start_t)));
            }
            if (mVoiceSourceListener != null && mUsbVoiceSource != null) {
                mVoiceSourceListener.onVoiceSourceChanged(null, ChangedReason.SCAN_TIMEOUT);
            }
        }
    };

    public synchronized void scanUsbDevices(Context context) {
        boolean isUsbVoiceExist = mUsbVoiceSource != null;
        if (LogUtil.DEBUG) {
            LogUtil.log(TAG, String.format("isUsbVoiceExist: %s", isUsbVoiceExist));
        }
        if (!isUsbVoiceExist) {
            start_t = System.currentTimeMillis();
            startScanTimer();
            UsbAudioService audioService = UsbAudioService.getInstance(context);
            audioService.setListener(mUsbListener);
            audioService.scanDevices();
        }
    }

    public synchronized void closeUsbVoiceSource() {
        boolean isUsbVoiceExist = mUsbVoiceSource != null;
        if (LogUtil.DEBUG) {
            LogUtil.log(TAG, String.format("isUsbVoiceExist: %s", isUsbVoiceExist));
        }
        if (isUsbVoiceExist) {
//            sAudioSource.closeDevice();
            mUsbVoiceSource = null;
        }
    }

    public synchronized void enableUsbDetection(Context context) {
        UsbAudioService.getInstance(context).setReqPermissionOnReceiver(true);
    }

    public synchronized void disableUsbDetection(Context context) {
        UsbAudioService.getInstance(context).setReqPermissionOnReceiver(false);
    }

    public synchronized UsbAudioSource getUsbVoiceSource() {
        return mUsbVoiceSource;
    }

    private synchronized void startScanTimer() {
        BackgroundThread.postDelayed(mUsbScanTimeoutTask, USB_SCAN_TIME_OUT_MS);
    }

    private synchronized void stopScanTimer() {
        BackgroundThread.getHandler().removeCallbacks(mUsbScanTimeoutTask);
    }


    public void setVoiceSourceListener(IVoiceSourceListener listener) {
        this.mVoiceSourceListener = listener;
    }

    public interface IVoiceSourceListener {
        void onVoiceSourceChanged(UsbAudioSource source, @ChangedReason int reason);
    }
}