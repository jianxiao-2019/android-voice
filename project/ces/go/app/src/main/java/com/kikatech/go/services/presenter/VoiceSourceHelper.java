package com.kikatech.go.services.presenter;

import android.content.Context;
import android.support.annotation.IntDef;

import com.kikatech.go.util.LogUtil;
import com.kikatech.usb.IUsbAudioListener;
import com.kikatech.usb.buffer.KikaBuffer;
import com.kikatech.usb.UsbAudioService;
import com.kikatech.usb.datasource.KikaGoVoiceSource;

/**
 * @author SkeeterWang Created on 2018/3/13.
 */

public class VoiceSourceHelper {
    private static final String TAG = "VoiceSourceHelper";

    private static final int EVENT_USB_ATTACHED = 1;
    private static final int EVENT_USB_DETACHED = 2;
    private static final int EVENT_USB_DEVICE_NOT_FOUND = 3;
    private static final int EVENT_USB_DEVICE_ERROR = 4;
    private static final int EVENT_NON_CHANGED = 5;

    @IntDef({EVENT_USB_ATTACHED, EVENT_USB_DETACHED, EVENT_USB_DEVICE_NOT_FOUND, EVENT_USB_DEVICE_ERROR, EVENT_NON_CHANGED})
    public @interface Event {
        int USB_ATTACHED = EVENT_USB_ATTACHED;
        int USB_DETACHED = EVENT_USB_DETACHED;
        int USB_DEVICE_NOT_FOUND = EVENT_USB_DEVICE_NOT_FOUND;
        int USB_DEVICE_ERROR = EVENT_USB_DEVICE_ERROR;
        int NON_CHANGED = EVENT_NON_CHANGED;
    }


    public static final String VOICE_SOURCE_ANDROID = "Android";
    public static final String VOICE_SOURCE_USB = "USB";


    private long start_t;

    private IVoiceSourceListener mVoiceSourceListener;
    private KikaGoUsbVoiceSourceWrapper mUsbVoiceSource;
    private IUsbAudioListener mUsbListener = new IUsbAudioListener() {
        @Override
        public void onDeviceAttached(KikaGoVoiceSource audioSource) {
            if (LogUtil.DEBUG) {
                LogUtil.logv(TAG, String.format("onDeviceAttached, spend: %s", (System.currentTimeMillis() - start_t)));
            }
            if (mUsbVoiceSource == null && audioSource != null) {
                mUsbVoiceSource = new KikaGoUsbVoiceSourceWrapper(audioSource);
                mUsbVoiceSource.setOnOpenedCallback(new KikaGoVoiceSource.OnOpenedCallback() {
                    @Override
                    public void onOpened(int state) {
                        if (LogUtil.DEBUG && mUsbVoiceSource != null) {
                            LogUtil.log(TAG, "fw version : 0x" + Integer.toHexString(mUsbVoiceSource.checkFwVersion())
                                    + " driver version : 0x" + Integer.toHexString(mUsbVoiceSource.checkDriverVersion()));
                        }
                    }
                });
                dispatchEvent(Event.USB_ATTACHED);
            } else {
                dispatchEvent(Event.NON_CHANGED);
            }
        }

        @Override
        public void onDeviceDetached() {
            if (LogUtil.DEBUG) {
                LogUtil.logv(TAG, String.format("onDeviceDetached, spend: %s", (System.currentTimeMillis() - start_t)));
            }
            if (mUsbVoiceSource != null) {
                clearUsbVoiceSource();
                dispatchEvent(Event.USB_DETACHED);
            } else {
                dispatchEvent(Event.NON_CHANGED);
            }
        }

        @Override
        public void onDeviceError(int errorCode) {
            if (LogUtil.DEBUG) {
                LogUtil.logw(TAG, String.format("onDeviceError, errorCode: %s", errorCode));
            }
            if (mUsbVoiceSource != null) {
                clearUsbVoiceSource();
                switch (errorCode) {
                    case IUsbAudioListener.ERROR_NO_DEVICES:
                        dispatchEvent(Event.USB_DEVICE_NOT_FOUND);
                        break;
                    default:
                        dispatchEvent(Event.USB_DEVICE_ERROR);
                        break;
                }
            } else {
                dispatchEvent(Event.NON_CHANGED);
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
            UsbAudioService audioService = UsbAudioService.getInstance(context);
            audioService.setListener(mUsbListener);
            audioService.scanDevices();
        } else {
            dispatchEvent(Event.NON_CHANGED);
        }
    }

    public synchronized void closeDevice(Context context) {
        UsbAudioService.getInstance(context).closeDevice();
    }

    private synchronized void clearUsbVoiceSource() {
        boolean isUsbVoiceExist = mUsbVoiceSource != null;
        if (LogUtil.DEBUG) {
            LogUtil.log(TAG, String.format("isUsbVoiceExist: %s", isUsbVoiceExist));
        }
        if (isUsbVoiceExist) {
            mUsbVoiceSource = null;
        }
    }

    public synchronized void setAudioFilePath(String path, String fileName) {
        if (mUsbVoiceSource != null) {
            mUsbVoiceSource.setAudioFilePath(path, fileName);
        }
    }

    public synchronized KikaGoUsbVoiceSourceWrapper getUsbVoiceSource() {
        return mUsbVoiceSource;
    }

    public synchronized void enableNoiseCancellation(int angle) {
        if (mUsbVoiceSource != null) {
            mUsbVoiceSource.updateBufferType(KikaBuffer.BufferType.NOISE_CANCELLATION);
            mUsbVoiceSource.setNoiseCancellationParameters(0, angle*2000);
        }
    }

    public synchronized void disableNoiseCancellation() {
        if (mUsbVoiceSource != null) {
            mUsbVoiceSource.updateBufferType(KikaBuffer.BufferType.STEREO_TO_MONO);
        }
    }

    public synchronized void usbVolumeUp() {
        if (LogUtil.DEBUG) {
            LogUtil.log(TAG, "usbVolumeUp");
        }
        setUsbVolume(6); // default volume level is 6
    }

    public synchronized void usbVolumeDown() {
        if (LogUtil.DEBUG) {
            LogUtil.log(TAG, "usbVolumeDown");
        }
        setUsbVolume(3); // min volume level is 3
    }

    private synchronized void setUsbVolume(int TARGET_VOLUME_LEVEL) {
        boolean isUsbVoiceExist = mUsbVoiceSource != null;
        if (LogUtil.DEBUG) {
            LogUtil.log(TAG, String.format("isUsbVoiceExist: %s", isUsbVoiceExist));
        }
        if (isUsbVoiceExist) {
            int currentVolumeLevel = mUsbVoiceSource.checkVolumeState();
            while (KikaGoVoiceSource.ERROR_VOLUME_FW_NOT_SUPPORT != currentVolumeLevel
                    && TARGET_VOLUME_LEVEL != currentVolumeLevel) {
                if (currentVolumeLevel > TARGET_VOLUME_LEVEL) {
                    currentVolumeLevel = mUsbVoiceSource.volumeDown();
                } else if (currentVolumeLevel < TARGET_VOLUME_LEVEL) {
                    currentVolumeLevel = mUsbVoiceSource.volumeUp();
                }
            }
        }
    }


    private synchronized void dispatchEvent(@Event int event) {
        if (mVoiceSourceListener != null) {
            mVoiceSourceListener.onVoiceSourceEvent(event);
        }
    }


    public void setVoiceSourceListener(IVoiceSourceListener listener) {
        this.mVoiceSourceListener = listener;
    }

    public interface IVoiceSourceListener {
        void onVoiceSourceEvent(@Event int event);
    }
}