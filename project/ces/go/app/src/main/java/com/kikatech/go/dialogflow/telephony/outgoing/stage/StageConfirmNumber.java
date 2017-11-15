package com.kikatech.go.dialogflow.telephony.outgoing.stage;

import android.os.Bundle;
import android.text.TextUtils;

import com.kikatech.go.dialogflow.telephony.outgoing.SceneActions;
import com.kikatech.go.util.LogUtil;
import com.kikatech.voice.core.dialogflow.scene.ISceneFeedback;
import com.kikatech.voice.core.dialogflow.scene.SceneBase;
import com.kikatech.voice.core.dialogflow.scene.SceneStage;
import com.kikatech.voice.util.contact.ContactManager;

import org.json.JSONObject;

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
                    String ordinal = parseOrdinal(extra);
                    if (!TextUtils.isEmpty(ordinal)) {
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
        SceneBase.OptionList optionList = null;
        if (mContact != null) {
            if (!mContact.phoneNumbers.isEmpty()) {
                speech = "Choose for the following list";
                optionList = new SceneBase.OptionList(SceneBase.OptionList.REQUEST_TYPE_ORDINAL);
                for (int i = 0; i < mContact.phoneNumbers.size(); i++) {
                    optionList.add(new SceneBase.Option(mContact.phoneNumbers.get(i), SceneActions.ACTION_OUTGOING_NUMBERS));
                }
            }
        }
        if (LogUtil.DEBUG) {
            LogUtil.logv(TAG, speech);
        }
        speak(speech, optionList);
    }

    private String parseOrdinal(Bundle param) {
        if (LogUtil.DEBUG) {
            LogUtil.log(TAG, param.toString());
        }
        String ordinal = null;
        for (String key : SceneActions.PARAM_OUTGOING_ORDINALS) {
            if (param.containsKey(key)) {
                ordinal = param.getString(key);
                break;
            }
        }
        if (!TextUtils.isEmpty(ordinal)) {
            ordinal = ordinal.substring(1, ordinal.length() - 1);
        }
        return ordinal;
    }

    private ContactManager.PhoneBookContact queryNumber(String ordinal) {
        try {
            if (!TextUtils.isEmpty(ordinal)) {
                int idxNumber = Integer.parseInt(ordinal) - 1;
                if (idxNumber < mContact.phoneNumbers.size()) {
                    return mContact.clone(idxNumber);
                }
            }
        } catch (Exception ignore) {
        }
        return null;
    }
}