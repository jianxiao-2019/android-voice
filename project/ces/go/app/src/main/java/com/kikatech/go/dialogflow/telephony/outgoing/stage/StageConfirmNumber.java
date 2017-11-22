package com.kikatech.go.dialogflow.telephony.outgoing.stage;

import android.os.Bundle;
import android.text.TextUtils;

import com.kikatech.go.dialogflow.model.Option;
import com.kikatech.go.dialogflow.model.OptionList;
import com.kikatech.go.dialogflow.telephony.outgoing.SceneActions;
import com.kikatech.go.util.LogUtil;
import com.kikatech.voice.core.dialogflow.scene.ISceneFeedback;
import com.kikatech.voice.core.dialogflow.scene.SceneBase;
import com.kikatech.voice.core.dialogflow.scene.SceneStage;
import com.kikatech.voice.util.contact.ContactManager;

import java.util.ArrayList;
import java.util.List;

/**
 * @author SkeeterWang Created on 2017/11/14.
 */
public class StageConfirmNumber extends StageOutgoing {
    private static final String TAG = "StageConfirmNumber";

    private ContactManager.PhoneBookContact mContact;
    private List<String> mOptions;

    public StageConfirmNumber(SceneBase scene, ISceneFeedback feedback, ContactManager.PhoneBookContact contact) {
        super(scene, feedback);
        mContact = contact;
        mOptions = new ArrayList<>();
    }

    @Override
    public SceneStage next(String action, Bundle extra) {
        if (!TextUtils.isEmpty(action)) {
            switch (action) {
                case SceneActions.ACTION_OUTGOING_NUMBERS:
                    return getNextStage(parseOrdinal(extra));
                case SceneActions.ACTION_OUTGOING_CANCEL:
                    return new StageCancel(mSceneBase, mFeedback);
                case SceneActions.ACTION_OUTGOING_CHANGE:
                    return getNextStage(parseOrdinalFromPhonePlace(extra));
                default:
                    return this;
            }
        }
        return null;
    }

    private SceneStage getNextStage(String ordinal) {
        if (!TextUtils.isEmpty(ordinal)) {
            ContactManager.PhoneBookContact newContact = queryNumber(ordinal);
            if (newContact != null) {
                return new StageMakeCall(mSceneBase, mFeedback, newContact);
            } else {
                return this;
            }
        }
        return this;
    }

    @Override
    public void action() {
        String speech = "error occurred, please contact RD";
        Bundle extras = null;
        if (mContact != null) {
            if (!mContact.phoneNumbers.isEmpty()) {
                extras = new Bundle();
                OptionList optionList = new OptionList(OptionList.REQUEST_TYPE_ORDINAL);
                optionList.setTitle("Which one do you like?");
                for (ContactManager.NumberType nt : mContact.phoneNumbers) {
                    String o = nt.getTypeOrNumber();
                    optionList.add(new Option(o, SceneActions.ACTION_OUTGOING_NUMBERS));
                    mOptions.add(o);
                }
                extras.putParcelable(EXTRA_OPTIONS_LIST, optionList);
                speech = optionList.getTextToSpeak();
            }
        }
        if (LogUtil.DEBUG) {
            LogUtil.logv(TAG, speech);
        }
        speak(speech, extras);
    }

    private String parseOrdinalFromPhonePlace(Bundle param) {
        if (LogUtil.DEBUG) {
            LogUtil.log(TAG, param.toString());
            LogUtil.log(TAG, "mOptions count:" + mOptions.size());
        }
        String option = param.getString("name", null);
        if (!TextUtils.isEmpty(option)) {
            option = option.replace("\"", "");
            LogUtil.log(TAG, "option:" + option);
            for (int i = 0; i < mOptions.size(); i++) {
                LogUtil.log(TAG, "option " + i + " : " + mOptions.get(i));
                if (mOptions.get(i).toLowerCase().equals(option)) {
                    LogUtil.log(TAG, "return:" + String.valueOf(i + 1));
                    return String.valueOf(i + 1);
                }
            }
        }
        return null;
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