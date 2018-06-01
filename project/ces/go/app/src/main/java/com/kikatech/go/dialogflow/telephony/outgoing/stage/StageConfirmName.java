package com.kikatech.go.dialogflow.telephony.outgoing.stage;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;

import com.kikatech.go.dialogflow.AsrConfigUtil;
import com.kikatech.go.dialogflow.SceneUtil;
import com.kikatech.go.dialogflow.model.ContactOptionList;
import com.kikatech.go.dialogflow.model.Option;
import com.kikatech.go.dialogflow.model.OptionList;
import com.kikatech.go.dialogflow.model.TtsText;
import com.kikatech.go.dialogflow.telephony.outgoing.SceneActions;
import com.kikatech.go.util.AppInfo;
import com.kikatech.go.util.LogUtil;
import com.kikatech.voice.core.dialogflow.scene.ISceneFeedback;
import com.kikatech.voice.core.dialogflow.scene.SceneBase;
import com.kikatech.voice.core.dialogflow.scene.SceneStage;
import com.kikatech.voice.util.contact.ContactManager;

/**
 * Created by tianli on 17-11-11.
 */

public class StageConfirmName extends StageOutgoing {
    private static final String TAG = "StageConfirmName";

    private ContactManager.PhoneBookContact mContact;

    StageConfirmName(SceneBase scene, ISceneFeedback feedback, ContactManager.PhoneBookContact contact) {
        super(scene, feedback);
        mContact = contact;
    }

    @Override
    protected int getAsrMode() {
        return AsrConfigUtil.ASR_MODE_CONVERSATION_CMD_ALTER;
    }

    @Override
    public SceneStage next(String action, Bundle extra) {
        if (!TextUtils.isEmpty(action)) {
            switch (action) {
                case SceneActions.ACTION_OUTGOING_YES:
                    if (mContact.phoneNumbers.size() > 1) {
                        return new StageConfirmNumber(mSceneBase, mFeedback, mContact);
                    } else {
                        return new StageMakeCall(mSceneBase, mFeedback, mContact);
                    }
                case SceneActions.ACTION_OUTGOING_NO:
                    return new StageAskName(mSceneBase, mFeedback);
                default:
                    return super.next(action, extra);
            }
        }
        return null;
    }

    @Override
    public void doAction() {
        onStageActionStart();
        action();
    }

    @Override
    public void action() {
        Context context = mSceneBase.getContext();
        String[] uiAndTtsText = SceneUtil.getConfirmContact(context, mContact.displayName);
        if (uiAndTtsText.length > 0) {
            String[] alignments = SceneUtil.getAlignmentCommon(context);
            requestAsrAlignment(alignments);
            String[] options = SceneUtil.getOptionsCommon(context);
            String uiText = uiAndTtsText[0];
            String ttsText = uiAndTtsText[1];
            ContactOptionList contactOptionList = new ContactOptionList(OptionList.REQUEST_TYPE_TEXT);
            contactOptionList.setTitle(uiText);
            contactOptionList.setAvatar(mContact.photoUri);
            contactOptionList.setAppInfo(AppInfo.PHONE);
            contactOptionList.setIconRes(SceneUtil.ICON_MSG);
            for (String option : options) {
                contactOptionList.add(new Option(option));
            }
            Bundle args = new Bundle();
            args.putParcelable(SceneUtil.EXTRA_CONTACT_OPTIONS_LIST, contactOptionList);
            speak(ttsText, args);
        }
    }
}
