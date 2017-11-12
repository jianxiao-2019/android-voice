package com.kikatech.go.dialogflow.telephony;

import android.content.Context;
import android.os.Bundle;

import com.kikatech.voice.core.dialogflow.DialogObserver;
import com.kikatech.voice.core.dialogflow.intent.Intent;
import com.kikatech.voice.core.dialogflow.scene.SceneBaseOld;

/**
 * @author SkeeterWang Created on 2017/11/6.
 */
public class SceneTelephonyIncoming extends SceneBaseOld implements DialogObserver {

    private static final String TAG = "SceneTelephonyIncoming";

    public static final String KIKA_PROCESS_INCOMING_CALL = "kika_process_incoming_call %s";

    private static final String ACTION_TELEPHONY_INCOMING_START = "telephony.incoming.start";
    private static final String ACTION_TELEPHONY_INCOMING_ANSWER = "telephony.incoming.answer";
    private static final String ACTION_TELEPHONY_INCOMING_REJECT = "telephony.incoming.reject";
    private static final String ACTION_TELEPHONY_INCOMING_IGNORE = "telephony.incoming.ignore";

    private final static String PRM_NAME = "name";

    private PhoneStateDispatcher mPhoneStateReceiver;

    public SceneTelephonyIncoming(Context context, ISceneCallback callback) {
        super(callback);
        registerBroadcastReceiver(context);
    }

    @Override
    public void onIntent(Intent intent) {
        processIntent(intent);
    }

    private void processIntent(Intent intent) {
        if (intent == null) {
            return;
        }

        final String action = intent.getAction();

        byte cmd;
        Bundle mCmdParams = null;
        switch (action) {
            case ACTION_TELEPHONY_INCOMING_START:
                cmd = TelephonyIncomingCommand.TELEPHONY_INCOMING_CMD_START;
                Bundle intentParams = intent.getExtra();
                if (intentParams != null && intentParams.containsKey(PRM_NAME)) {
                    mCmdParams = new Bundle();
                    mCmdParams.putString(TelephonyIncomingCommand.TELEPHONY_INCOMING_CMD_NAME, intentParams.getString(PRM_NAME));
                }
                break;
            case ACTION_TELEPHONY_INCOMING_ANSWER:
                cmd = TelephonyIncomingCommand.TELEPHONY_INCOMING_CMD_ANSWER;
                break;
            case ACTION_TELEPHONY_INCOMING_REJECT:
                cmd = TelephonyIncomingCommand.TELEPHONY_INCOMING_CMD_REJECT;
                break;
            case ACTION_TELEPHONY_INCOMING_IGNORE:
                cmd = TelephonyIncomingCommand.TELEPHONY_INCOMING_CMD_IGNORE;
                break;
            default:
                if (mPhoneStateReceiver != null && mPhoneStateReceiver.isIncomingCalling()) {
                    callbackPreStartTelephonyIncoming(mPhoneStateReceiver.getNumber());
                }
                return;
        }

        if (mCallback != null) {
            mCallback.onCommand(cmd, mCmdParams);
        }
    }

    private void registerBroadcastReceiver(Context context) {
        mPhoneStateReceiver = new PhoneStateDispatcher(new PhoneStateDispatcher.ICallStateChangeListener() {
            @Override
            public void onInComingCallRinging(String phoneNumber) {
                callbackPreStartTelephonyIncoming( phoneNumber );
            }

            @Override
            public void onInComingCallEnded() {
                if (mCallback != null) {
                    mCallback.resetContextImpl();
                }
            }
        });
        mPhoneStateReceiver.register(context);
    }

    public void unregisterBroadcastReceiver(Context context) {
        mPhoneStateReceiver.unregister(context);
    }

    private void callbackPreStartTelephonyIncoming(String phoneNumber) {
        if (mCallback != null) {
            mCallback.resetContextImpl();
            Bundle mCmdParams = new Bundle();
            mCmdParams.putString(TelephonyIncomingCommand.TELEPHONY_INCOMING_CMD_NAME, phoneNumber);
            mCallback.onCommand(TelephonyIncomingCommand.TELEPHONY_INCOMING_CMD_PRE_START, mCmdParams);
        }
    }
}
