package com.kikatech.go.dialogflow.telephony;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.telephony.TelephonyManager;

import com.kikatech.voice.util.log.LogUtil;

/**
 * @author SkeeterWang Created on 2017/10/16.
 */
public class PhoneStateReceiver extends BroadcastReceiver {
    private static final String TAG = "SysPhoneStateReceiver";

    public static final String ACTION_PHONE_STATE = "action_kika_phone_state";

    static final class Actions {
        static final String ACTION_INCOMING_CALL_RINGING = "action_incoming_call_ringing";
        static final String ACTION_INCOMING_CALL_OFFHOOK = "action_incoming_call_offhook";
        static final String ACTION_INCOMING_CALL_IDLE = "action_incoming_call_idle";
        static final String ACTION_OUTGOING_CALL = "action_outgoing_call";
        static final String ACTION_OUTGOING_CALL_IDLE = "action_outgoing_call_idle";
    }

    static final class ArgKeys {
        static final String KEY_ACTION = "key_action";
        static final String KEY_PHONE_NUMBER = "key_phone_number";
    }

    private static boolean isIncomingCall;

    @Override
    public void onReceive(final Context context, Intent intent) {
        if (LogUtil.DEBUG) LogUtil.log(TAG, "onReceive");

        final TelephonyManager tm = (TelephonyManager) context.getSystemService(Service.TELEPHONY_SERVICE);

        Intent broadcastIntent = new Intent(ACTION_PHONE_STATE);

        switch (tm.getCallState()) {
            case TelephonyManager.CALL_STATE_OFFHOOK: // 电话打进来接通状态；电话打出时首先监听到的状态。
                if (LogUtil.DEBUG) LogUtil.log(TAG, "[onCallStateChanged] CALL_STATE_OFFHOOK");
                if (isIncomingCall) {
                    if (LogUtil.DEBUG) LogUtil.logd(TAG, "incoming call offhook");
                    broadcastIntent.putExtra(ArgKeys.KEY_ACTION, Actions.ACTION_INCOMING_CALL_OFFHOOK);
                } else {
                    if (LogUtil.DEBUG) LogUtil.logd(TAG, "outgoing call offhook");
                    broadcastIntent.putExtra(ArgKeys.KEY_ACTION, Actions.ACTION_OUTGOING_CALL);
                }
                LocalBroadcastManager.getInstance(context).sendBroadcast(broadcastIntent);
                break;
            case TelephonyManager.CALL_STATE_RINGING: // 电话打进来状态
                if (LogUtil.DEBUG) LogUtil.log(TAG, "[onCallStateChanged] CALL_STATE_RINGING");
                isIncomingCall = true;
                broadcastIntent.putExtra(ArgKeys.KEY_PHONE_NUMBER, intent.getStringExtra("incoming_number"));
                broadcastIntent.putExtra(ArgKeys.KEY_ACTION, Actions.ACTION_INCOMING_CALL_RINGING);
                LocalBroadcastManager.getInstance(context).sendBroadcast(broadcastIntent);
                break;
            case TelephonyManager.CALL_STATE_IDLE: // 不管是电话打出去还是电话打进来都会监听到的状态。
                if (LogUtil.DEBUG) LogUtil.log(TAG, "[onCallStateChanged] CALL_STATE_IDLE");
                if (isIncomingCall) {
                    broadcastIntent.putExtra(ArgKeys.KEY_ACTION, Actions.ACTION_INCOMING_CALL_IDLE);
                } else {
                    broadcastIntent.putExtra(ArgKeys.KEY_ACTION, Actions.ACTION_OUTGOING_CALL_IDLE);
                }
                LocalBroadcastManager.getInstance(context).sendBroadcast(broadcastIntent);
                isIncomingCall = false;
                break;
        }
    }
}