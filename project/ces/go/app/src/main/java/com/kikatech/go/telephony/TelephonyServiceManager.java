package com.kikatech.go.telephony;

import android.content.Context;
import android.media.AudioManager;

import com.kikatech.go.telephony.services.BaseTelephonyService;
import com.kikatech.go.telephony.services.TelephonyServiceHeadset;
import com.kikatech.go.telephony.services.TelephonyServiceHeadsetNLS;
import com.kikatech.go.util.DeviceUtil;
import com.kikatech.go.util.LogUtil;

/**
 * @author SkeeterWang Created on 2017/10/26.
 */
public class TelephonyServiceManager {
    private static final String TAG = "TelephonyServiceManager";

    private static TelephonyServiceManager sIns;
    private static BaseTelephonyService mTelephonyService;

    public static synchronized TelephonyServiceManager getIns() {
        if (sIns == null) {
            sIns = new TelephonyServiceManager();
        }
        return sIns;
    }

    private TelephonyServiceManager() {
        if (DeviceUtil.overLollipop()) {
            mTelephonyService = new TelephonyServiceHeadsetNLS();
        } else {
            mTelephonyService = new TelephonyServiceHeadset();
        }
    }


    public void makePhoneCall(Context context, String number) {
        if (LogUtil.DEBUG) LogUtil.log(TAG, "makePhoneCall, number: " + number);

        if (mTelephonyService != null) {
            mTelephonyService.makePhoneCall(context, number);
        }
    }

    public void answerPhoneCall(Context context) {
        if (LogUtil.DEBUG) LogUtil.log(TAG, "answerPhoneCall");

        if (mTelephonyService != null) {
            mTelephonyService.answerPhoneCall(context);
        }
    }

    public void killPhoneCall(Context context) {
        if (LogUtil.DEBUG) LogUtil.log(TAG, "killPhoneCall");

        if (mTelephonyService != null) {
            mTelephonyService.killPhoneCall(context);
        }
    }


    public void turnOnSpeaker(Context context) {
        if (LogUtil.DEBUG) {
            LogUtil.log(TAG, "turnOnSpeaker");
        }

        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);

        if (audioManager == null) {
            if (LogUtil.DEBUG) {
                LogUtil.logw(TAG, "audioManager is null");
            }
            return;
        }

        audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);

        if (!audioManager.isSpeakerphoneOn()) {
            audioManager.setSpeakerphoneOn(true);
            /*
            audioManager.setStreamVolume( AudioManager.STREAM_VOICE_CALL,
										  audioManager.getStreamMaxVolume( AudioManager.STREAM_VOICE_CALL ),
										  AudioManager.STREAM_VOICE_CALL );
										  */
        }
    }

    public void turnOffSpeaker(Context context) {
        if (LogUtil.DEBUG) {
            LogUtil.log(TAG, "turnOffSpeaker");
        }

        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);

        if (audioManager == null) {
            if (LogUtil.DEBUG) {
                LogUtil.logw(TAG, "audioManager is null");
            }
            return;
        }

        audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);

        if (audioManager.isSpeakerphoneOn()) {
            audioManager.setSpeakerphoneOn(false);
        }

        audioManager.setMode(AudioManager.MODE_NORMAL);
    }

    public boolean isSpeakerOn(Context context) {
        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        return audioManager.isSpeakerphoneOn();
    }

    public void turnOnSilentMode(Context context) {
        if (LogUtil.DEBUG) LogUtil.log(TAG, "turnOnSilentMode");
        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        audioManager.setRingerMode(AudioManager.RINGER_MODE_SILENT);
    }

    public void turnOffSilentMode(Context context) {
        if (LogUtil.DEBUG) LogUtil.log(TAG, "turnOffSilentMode");
        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        audioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
    }
}
