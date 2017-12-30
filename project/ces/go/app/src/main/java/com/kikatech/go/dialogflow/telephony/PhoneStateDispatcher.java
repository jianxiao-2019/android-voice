package com.kikatech.go.dialogflow.telephony;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;

/**
 * @author SkeeterWang Created on 2017/11/7.
 */
public class PhoneStateDispatcher extends BroadcastReceiver {

    private static final String TAG = "PhoneStateDispatcher";

    private ICallStateChangeListener mListener;
    private boolean isIncomingCalling;
    private String mNumber;

    public PhoneStateDispatcher(ICallStateChangeListener listener) {
        this.mListener = listener;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null) {
            return;
        }
        String action = intent.getStringExtra(PhoneStateReceiver.ArgKeys.KEY_ACTION);
        if (TextUtils.isEmpty(action)) {
            return;
        }
        switch (action) {
            case PhoneStateReceiver.Actions.ACTION_INCOMING_CALL_RINGING:
                String number = intent.getStringExtra(PhoneStateReceiver.ArgKeys.KEY_PHONE_NUMBER);
                isIncomingCalling = true;
                mNumber = number;
                if (mListener != null) {
                    mListener.onInComingCallRinging(number);
                }
                break;
            case PhoneStateReceiver.Actions.ACTION_INCOMING_CALL_OFFHOOK:
                resetState();
                break;
            case PhoneStateReceiver.Actions.ACTION_INCOMING_CALL_IDLE:
                resetState();
                if (mListener != null) {
                    mListener.onInComingCallEnded();
                }
                break;
            case PhoneStateReceiver.Actions.ACTION_OUTGOING_CALL:
                resetState();
                if (mListener != null) {
                    mListener.onOutgoingCallRinging();
                }
                break;
            case PhoneStateReceiver.Actions.ACTION_OUTGOING_CALL_IDLE:
                resetState();
                if (mListener != null) {
                    mListener.onOutgoingCallEnded();
                }
                break;
        }
    }

    private void resetState() {
        isIncomingCalling = false;
        mNumber = null;
    }

    public void register(Context context) {
        try {
            LocalBroadcastManager.getInstance(context).registerReceiver(PhoneStateDispatcher.this,
                    new IntentFilter(PhoneStateReceiver.ACTION_PHONE_STATE));
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void unregister(Context context) {
        try {
            LocalBroadcastManager.getInstance(context).unregisterReceiver(PhoneStateDispatcher.this);
        } catch (Exception e) {
            e.printStackTrace();
        }
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

        void onOutgoingCallRinging();

        void onOutgoingCallEnded();
    }
}
