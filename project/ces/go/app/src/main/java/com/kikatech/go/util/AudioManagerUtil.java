package com.kikatech.go.util;

import android.content.Context;
import android.media.AudioManager;
import android.os.Build;
import android.support.annotation.RequiresApi;

import com.kikatech.go.ui.KikaMultiDexApplication;

import java.lang.reflect.Method;

/**
 * @author SkeeterWang Created on 2017/11/27.
 */

public class AudioManagerUtil {
    private static final String TAG = "AudioManagerUtil";

    private enum StreamType {
        STREAM_MUSIC(AudioManager.STREAM_MUSIC),
        STREAM_ALARM(AudioManager.STREAM_ALARM),
        STREAM_SYSTEM(AudioManager.STREAM_SYSTEM),
        STREAM_NOTIFICATION(AudioManager.STREAM_NOTIFICATION),
        STREAM_RING(AudioManager.STREAM_RING),
        STREAM_DTMF(AudioManager.STREAM_DTMF);

        private int type;
        private boolean isMuted;

        StreamType(int type) {
            this.type = type;
        }

        boolean equals(StreamType target) {
            return this.type == target.type;
        }
    }

    private static AudioManagerUtil sIns;
    private AudioManager mAudioManager;
    private static boolean isControlledRing;

    public static synchronized AudioManagerUtil getIns() {
        if (sIns == null) {
            sIns = new AudioManagerUtil();
        }
        return sIns;
    }

    private AudioManagerUtil() {
        mAudioManager = (AudioManager) KikaMultiDexApplication.getAppContext().getSystemService(Context.AUDIO_SERVICE);
    }


    public synchronized void muteRing() {
        if (isWiredHeadsetOn()) return;
        if (!isControlledRing) {
            switch (mAudioManager.getRingerMode()) {
                case AudioManager.RINGER_MODE_NORMAL:
                    if (LogUtil.DEBUG) LogUtil.logd(TAG, "RINGER_MODE_NORMAL");
                    isControlledRing = true;
                    configStreamStatus();
                    mute(StreamType.STREAM_RING);
                    break;
                case AudioManager.RINGER_MODE_SILENT:
                    if (LogUtil.DEBUG) LogUtil.logd(TAG, "RINGER_MODE_SILENT");
                    isControlledRing = false;
                    break;
                case AudioManager.RINGER_MODE_VIBRATE:
                    if (LogUtil.DEBUG) LogUtil.logd(TAG, "RINGER_MODE_VIBRATE");
                    isControlledRing = false;
                    break;
            }
        }
    }

    public synchronized void unmuteRing() {
        if (isControlledRing) {
            unmute(StreamType.STREAM_RING);
            isControlledRing = false;
        }
    }

    private synchronized void configStreamStatus() {
        for (StreamType streamType : StreamType.values()) {
            boolean isStreamMute = isStreamMute(streamType);
            if (LogUtil.DEBUG) {
                LogUtil.logv(TAG, "[" + streamType.name() + "] isStreamMute? " + isStreamMute);
            }
            streamType.isMuted = isStreamMute;
        }
    }

    private boolean isStreamMute(StreamType streamType) {
        if (DeviceUtil.overM()) {
            return isStreamMuteAboveM(streamType);
        } else {
            return isStreamMuteBelowM(streamType);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private boolean isStreamMuteAboveM(StreamType streamType) {
        try {
            return mAudioManager.isStreamMute(streamType.type);
        } catch (Exception e) {
            if (LogUtil.DEBUG) LogUtil.printStackTrace(TAG, e.getMessage(), e);
        }
        return isStreamMuteBelowM(streamType);
    }

    private boolean isStreamMuteBelowM(StreamType streamType) {
        try {
            Method method = AudioManager.class.getMethod("isStreamMute", int.class);
            return (boolean) method.invoke(mAudioManager, streamType.type);
        } catch (Exception e) {
            if (LogUtil.DEBUG) {
                LogUtil.printStackTrace(TAG, e.getMessage(), e);
            }
        }
        return false;
    }


    private boolean isWiredHeadsetOn() {
        return mAudioManager.isWiredHeadsetOn();
    }


    private void mute(StreamType streamType) {
        if (LogUtil.DEBUG) {
            LogUtil.logv(TAG, "[mute] " + "Stream: " + streamType.name() + ", isMuted: " + streamType.isMuted);
        }
        if (!streamType.isMuted) {
            if (DeviceUtil.overM()) {
                mAudioManager.adjustStreamVolume(streamType.type, AudioManager.ADJUST_MUTE, 0);
            } else {
                mAudioManager.setStreamMute(streamType.type, true);
            }
        }
    }

    private void unmute(StreamType streamType) {
        if (LogUtil.DEBUG) {
            LogUtil.logv(TAG, "[unmute] " + "Stream: " + streamType.name() + ", isMuted: " + streamType.isMuted);
        }
        if (!streamType.isMuted) {
            if (DeviceUtil.overM()) {
                mAudioManager.adjustStreamVolume(streamType.type, AudioManager.ADJUST_UNMUTE, 0);
            } else {
                mAudioManager.setStreamMute(streamType.type, false);
            }
        }
    }

    private void muteAll() {
        for (StreamType streamType : StreamType.values()) {
            mute(streamType);
        }
    }

    private void unmuteAll(final IMuteActionCallback callback) {
        BackgroundThread.getHandler().postDelayed(new Runnable() {
            @Override
            public void run() {
                for (StreamType streamType : StreamType.values()) {
                    unmute(streamType);
                }
                if (callback != null) {
                    callback.onDone();
                }
            }
        }, 1000);
    }

    private void muteAllExpect(final StreamType... typesToExpects) {
        for (StreamType streamType : StreamType.values()) {
            boolean isExpectType = isExpectType(streamType, typesToExpects);
            if (LogUtil.DEBUG) LogUtil.logv(TAG, "isExpectType? " + isExpectType);
            if (!isExpectType) {
                mute(streamType);
            }
        }
    }

    private boolean isExpectType(StreamType targetType, StreamType... typesToExpects) {
        for (StreamType typesToExpect : typesToExpects) {
            if (targetType.type == typesToExpect.type) {
                return true;
            }
        }
        return false;
    }


    private interface IMuteActionCallback {
        void onDone();
    }
}
