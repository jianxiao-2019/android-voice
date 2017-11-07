package com.kikatech.voice.core.dialogflow.telephony;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;

/**
 * @author SkeeterWang Created on 2017/11/7.
 */
public class KikaPhoneStateReceiver extends BroadcastReceiver {
    private static final String TAG = "KikaPhoneStateReceiver";

    private ICallStateChangeListener mListener;
    private boolean isIncomingCalling;
    private String mNumber;

    public KikaPhoneStateReceiver(ICallStateChangeListener listener) {
        this.mListener = listener;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null) {
            return;
        }
        String action = intent.getStringExtra(SysPhoneStateReceiver.ArgKeys.KEY_ACTION);
        if (TextUtils.isEmpty(action)) {
            return;
        }
        switch (action) {
            case SysPhoneStateReceiver.Actions.ACTION_INCOMING_CALL_RINGING:
                String number = intent.getStringExtra(SysPhoneStateReceiver.ArgKeys.KEY_PHONE_NUMBER);
                isIncomingCalling = true;
                mNumber = number;
                if (mListener != null) {
                    mListener.onInComingCallRinging(number);
                }
                break;
            case SysPhoneStateReceiver.Actions.ACTION_INCOMING_CALL_OFFHOOK:
                resetState();
                break;
            case SysPhoneStateReceiver.Actions.ACTION_INCOMING_CALL_IDLE:
                resetState();
                if (mListener != null) {
                    mListener.onInComingCallEnded();
                }
                break;
            case SysPhoneStateReceiver.Actions.ACTION_OUTGOING_CALL:
            case SysPhoneStateReceiver.Actions.ACTION_OUTGOING_CALL_IDLE:
                resetState();
                break;
        }
    }

    private void resetState() {
        isIncomingCalling = false;
        mNumber = null;
    }

    public void register(Context context) {
        LocalBroadcastManager.getInstance(context).registerReceiver(KikaPhoneStateReceiver.this, new IntentFilter(SysPhoneStateReceiver.ACTION_PHONE_STATE));
    }

    public void unregister(Context context) {
        LocalBroadcastManager.getInstance(context).unregisterReceiver(KikaPhoneStateReceiver.this);
    }

    public boolean isIncomingCalling() {
        return isIncomingCalling;
    }

    public String getNumber() {
        return mNumber;
    }

    public interface ICallStateChangeListener {
        void onInComingCallRinging(String phoneNumber);
        void onInComingCallEnded();
    }
}
