package com.kikatech.go.dialogflow.telephony.outgoing.stage;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;

import com.kikatech.go.dialogflow.AsrConfigUtil;
import com.kikatech.go.dialogflow.SceneUtil;
import com.kikatech.go.dialogflow.model.Option;
import com.kikatech.go.dialogflow.model.OptionList;
import com.kikatech.go.dialogflow.telephony.outgoing.SceneActions;
import com.kikatech.go.util.LogUtil;
import com.kikatech.voice.core.dialogflow.intent.Intent;
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

    private ContactManager.PhoneBookContact mContact;
    private List<String> mOptions;

    StageConfirmNumber(SceneBase scene, ISceneFeedback feedback, ContactManager.PhoneBookContact contact) {
        super(scene, feedback);
        mContact = contact;
        mOptions = new ArrayList<>();
        overrideUncaughtAction = true;
    }

    @Override
    protected int getAsrMode() {
        return AsrConfigUtil.ASR_MODE_CONVERSATION_CMD_ALTER;
    }

    @Override
    public SceneStage next(String action, Bundle extra) {
        if (!TextUtils.isEmpty(action)) {
            switch (action) {
                case SceneActions.ACTION_OUTGOING_NUMBERS:
                    return getNextStage(parseOrdinal(extra));
                case SceneActions.ACTION_OUTGOING_CANCEL:
                    return new StageCancel(mSceneBase, mFeedback);
                case Intent.ACTION_UNKNOWN:
                    String[] nBest = Intent.parseUserInputNBest(extra);
                    if (nBest != null && nBest.length > 0) {
                        for (String target : nBest) {
                            for (int i = 0; i < mContact.phoneNumbers.size(); i++) {
                                ContactManager.NumberType nt = mContact.phoneNumbers.get(i);
                                if (target.toLowerCase().contains(nt.type.toLowerCase())) {
                                    if (LogUtil.DEBUG) {
                                        LogUtil.log(TAG, "Hit " + target);
                                    }
                                    return getNextStage(String.valueOf(i + 1));
                                }
                            }
                        }
                    }
                    return this;
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
    public void doAction() {
        onStageActionStart();
        action();
    }

    @Override
    public void action() {
        String speech = "error occurred, please contact RD";
        Bundle extras = null;
        if (mContact != null) {
            if (!mContact.phoneNumbers.isEmpty()) {
                Context context = mSceneBase.getContext();
                String[] uiAndTtsText = SceneUtil.getOptionListCommon(context);
                if (uiAndTtsText.length > 0) {
                    extras = new Bundle();
                    String uiText = uiAndTtsText[0];
                    String ttsText = uiAndTtsText[1];
                    OptionList optionList = new OptionList(OptionList.REQUEST_TYPE_ORDINAL_TO_TEXT);
                    optionList.setTitle(uiText);
                    optionList.setIconRes(SceneUtil.ICON_TELEPHONY);
                    int iteratorSize = mContact.phoneNumbers.size() > 2 ? 2 : mContact.phoneNumbers.size();
                    for (int i = 0; i < iteratorSize; i++) {
                        ContactManager.NumberType nt = mContact.phoneNumbers.get(i);
                        String option = nt.getTypeOrNumber();
                        optionList.add(new Option(option));
                        mOptions.add(option);
                    }
                    extras.putParcelable(SceneUtil.EXTRA_OPTIONS_LIST, optionList);
                    speech = optionList.getTextToSpeak(ttsText);
                }
            }
        }
        if (LogUtil.DEBUG) {
            LogUtil.logv(TAG, speech);
        }
        speak(speech, extras);
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