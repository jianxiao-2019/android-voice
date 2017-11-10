package com.kikatech.voice.core.dialogflow.scene;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;

import com.kikatech.voice.core.dialogflow.DialogObserver;
import com.kikatech.voice.core.dialogflow.constant.TelephonyOutgoingCommand;
import com.kikatech.voice.core.dialogflow.intent.Intent;
import com.kikatech.voice.util.contact.ContactManager;
import com.kikatech.voice.util.log.LogUtil;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author SkeeterWang Created on 2017/11/8.
 */
public class SceneTelephonyOutgoing extends SceneBase implements DialogObserver {
    private static final String TAG = "SceneTelephonyOutgoing";

    private static final String ACTION_TELEPHONY_OUTGOING_START = "telephony.outgoing.start";
    private static final String ACTION_TELEPHONY_OUTGOING_YES = "telephony.outgoing.yes";
    private static final String ACTION_TELEPHONY_OUTGOING_NO = "telephony.outgoing.no";
    private static final String ACTION_TELEPHONY_OUTGOING_CHANGE = "telephony.outgoing.change";
    private static final String ACTION_TELEPHONY_OUTGOING_CANCEL = "telephony.outgoing.cancel";

    private static final byte NAME_STATE_NULL = 0x01;
    private static final byte NAME_STATE_FULL_MACH = 0x02;
    private static final byte NAME_STATE_FUZZY_MATCH = 0x03;
    private static final byte NAME_STATE_NOT_FOUND = 0x04;

    private final static String PRM_NAME = "name";

    private Context mContext;
    private boolean mOutgoingStart;
    private int mOutgoingNameState;
    private boolean mOutGoingNameConfirm;
    private ContactManager.PhoneBookContact mOutGoingContact;

    private final Bundle mCmdParams = new Bundle();

    public SceneTelephonyOutgoing(Context context, ISceneCallback callback) {
        super(callback);
        mContext = context;
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

        switch (action) {
            case ACTION_TELEPHONY_OUTGOING_START:
                mOutgoingStart = true;
                parsingName(intent.getExtra());
                break;
            case ACTION_TELEPHONY_OUTGOING_YES:
                if (mOutgoingStart) {
                    mOutGoingNameConfirm = true;
                }
                break;
            case ACTION_TELEPHONY_OUTGOING_NO:
                break;
            case ACTION_TELEPHONY_OUTGOING_CHANGE:
                parsingName(intent.getExtra());
                break;
            case ACTION_TELEPHONY_OUTGOING_CANCEL:
                resetContext();
                break;
            case ACTION_UNKNOWN:
                break;
            default:
                break;
        }

        byte cmd = checkState(action);

        if (LogUtil.DEBUG) LogUtil.log(TAG, "processIntent, cmd:" + cmd);

        if (mCallback != null) {
            mCmdParams.clear();
            if (mOutGoingContact != null) {
                mCmdParams.putString(TelephonyOutgoingCommand.TELEPHONY_OUTGOING_CMD_NAME, mOutGoingContact.displayName);
                mCmdParams.putString(TelephonyOutgoingCommand.TELEPHONY_OUTGOING_CMD_NUMBER, mOutGoingContact.phoneNumber);
            }
            mCallback.onCommand(cmd, mCmdParams);
        }

        switch (cmd) {
            case TelephonyOutgoingCommand.TELEPHONY_OUTGOING_CMD_START_CALL:
                resetContext();
                break;
        }
    }

    private void resetContext() {
        if (LogUtil.DEBUG) LogUtil.log(TAG, "resetContext >>>>>>> ");
        if (mCallback != null) mCallback.resetContextImpl();
        resetVariables();
    }

    private void resetVariables() {
        mOutgoingStart = false;
        mOutgoingNameState = NAME_STATE_NULL;
        mOutGoingNameConfirm = false;
        mOutGoingContact = null;
    }

    private void parsingName(Bundle params) {
        mOutGoingContact = null;
        mOutGoingNameConfirm = false;
        mOutgoingNameState = NAME_STATE_NULL;
        if (params != null && params.containsKey(PRM_NAME)) {
            String name = params.getString(PRM_NAME);
            if (!TextUtils.isEmpty(name)) {
                try {
                    name = name.substring(1, name.length() - 1);
                } catch (Exception ignore) {
                }
                mOutGoingContact = ContactManager.getIns().findName(mContext, name);
                if (mOutGoingContact != null) {
                    if (LogUtil.DEBUG) {
                        LogUtil.log(TAG, "name: " + name + ", displayName: " + mOutGoingContact.displayName);
                    }
                    if (name.equals(mOutGoingContact.displayName)) {
                        if (mOutgoingStart) {
                            mOutGoingNameConfirm = true;
                        }
                        mOutgoingNameState = NAME_STATE_FULL_MACH;
                    } else {
                        mOutgoingNameState = NAME_STATE_FUZZY_MATCH;
                    }
                } else {
                    mOutgoingNameState = NAME_STATE_NOT_FOUND;
                }
            }
        }
    }

    private byte checkState(String action) {
        // Check special cases first
        switch (action) {
            case ACTION_TELEPHONY_OUTGOING_NO:
                if (LogUtil.DEBUG) LogUtil.log(TAG, "[SC] Ask name again");
                return TelephonyOutgoingCommand.TELEPHONY_OUTGOING_CMD_ASK_NAME;
            case ACTION_TELEPHONY_OUTGOING_CANCEL:
                if (LogUtil.DEBUG) LogUtil.log(TAG, "[SC] Canceled phone call.");
                return TelephonyOutgoingCommand.TELEPHONY_OUTGOING_CMD_CANCELED;
            case ACTION_UNKNOWN:
                if (LogUtil.DEBUG) LogUtil.log(TAG, "[SC] Cannot understand what user says");
                return TelephonyOutgoingCommand.TELEPHONY_OUTGOING_CMD_DONT_UNDERSTAND;
            default:
                break;
        }

        if (!mOutgoingStart) {
            if (LogUtil.DEBUG) LogUtil.log(TAG, "[Err] Telephony outgoing is not started yet");
            return TelephonyOutgoingCommand.TELEPHONY_OUTGOING_CMD_ERR;
        }

        if (mOutGoingContact == null || TextUtils.isEmpty(mOutGoingContact.displayName)) {
            if (LogUtil.DEBUG) LogUtil.log(TAG, "[action] Ask name");
            switch (mOutgoingNameState) {
                case NAME_STATE_NULL:
                    return TelephonyOutgoingCommand.TELEPHONY_OUTGOING_CMD_ASK_NAME;
                case NAME_STATE_NOT_FOUND:
                    return TelephonyOutgoingCommand.TELEPHONY_OUTGOING_CMD_CONTACT_NOT_FOUND;
            }
        }

        if (mOutGoingNameConfirm) {
            if (LogUtil.DEBUG) LogUtil.log(TAG, "[action] Start phone call");
            return TelephonyOutgoingCommand.TELEPHONY_OUTGOING_CMD_START_CALL;
        } else {
            if (LogUtil.DEBUG) LogUtil.log(TAG, "[action] Confirm if given name is correct");
            return TelephonyOutgoingCommand.TELEPHONY_OUTGOING_CMD_CONFIRM_NAME;
        }
    }
}
