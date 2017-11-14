package com.kikatech.go.dialogflow.telephony.outgoing.stage;

import android.os.Bundle;
import android.text.TextUtils;

import com.kikatech.go.dialogflow.telephony.outgoing.SceneActions;
import com.kikatech.go.util.LogUtil;
import com.kikatech.voice.core.dialogflow.scene.ISceneFeedback;
import com.kikatech.voice.core.dialogflow.scene.SceneBase;
import com.kikatech.voice.core.dialogflow.scene.SceneStage;
import com.kikatech.voice.util.contact.ContactManager;

/**
 * @author SkeeterWang Created on 2017/11/14.
 */
public class StageConfirmNumber extends StageOutgoing {
    private static final String TAG = "StageConfirmNumber";

    private ContactManager.PhoneBookContact mContact;

    public StageConfirmNumber(SceneBase scene, ISceneFeedback feedback, ContactManager.PhoneBookContact contact) {
        super(scene, feedback);
        mContact = contact;
    }

    @Override
    public SceneStage next(String action, Bundle extra) {
        if (!TextUtils.isEmpty(action)) {
            switch (action) {
                case SceneActions.ACTION_OUTGOING_NUMBERS:
                    if (extra.containsKey(SceneActions.PARAM_OUTGOING_ORDINAL)) {
                        String ordinal = extra.getString(SceneActions.PARAM_OUTGOING_ORDINAL);
                        ContactManager.PhoneBookContact newContact = queryNumber(ordinal);
                        if (newContact != null) {
                            return new StageMakeCall(mSceneBase, mFeedback, newContact);
                        } else {
                            return this;
                        }
                    }
                    break;
                case SceneActions.ACTION_OUTGOING_CANCEL:
                    return new StageCancel(mSceneBase, mFeedback);
                default:
                    return this;
            }
        }
        return null;
    }

    @Override
    public void action() {
        String speech = "error occurred, please contact RD";
        if (mContact != null) {
            if (!mContact.phoneNumbers.isEmpty()) {
                speech = "Choose for the following list\n";
                for (int i = 0; i < mContact.phoneNumbers.size(); i++) {
                    speech += String.format("%1$s %2$s\n", i + 1, mContact.phoneNumbers.get(i));
                }
            }
        }
        if (LogUtil.DEBUG) {
            LogUtil.logv(TAG, speech);
        }
        speak(speech);
    }

    private ContactManager.PhoneBookContact queryNumber(String ordinal) {
        try {
            if (!TextUtils.isEmpty(ordinal)) {
                int idxNumber = Integer.parseInt(ordinal.substring(1, ordinal.length() - 1)) - 1;
                if (idxNumber < mContact.phoneNumbers.size()) {
                    return mContact.clone(idxNumber);
                }
            }
        } catch (Exception ignore) {
        }
        return null;
    }
}