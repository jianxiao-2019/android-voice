package com.kikatech.go.dialogflow.telephony.outgoing.stage;

import android.os.Bundle;
import android.text.TextUtils;

import com.kikatech.go.dialogflow.SceneUtil;
import com.kikatech.go.dialogflow.telephony.outgoing.SceneActions;
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

    public StageConfirmName(SceneBase scene, ISceneFeedback feedback, ContactManager.PhoneBookContact contact) {
        super(scene, feedback);
        mContact = contact;
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
        action();
    }

    @Override
    public void action() {
        String[] uiAndTtsText = SceneUtil.getConfirmContact(mSceneBase.getContext(), mContact.displayName);
        if (uiAndTtsText.length > 0) {
            String uiText = uiAndTtsText[0];
            String ttsText = uiAndTtsText[1];
            Bundle args = new Bundle();
            args.putString(SceneUtil.EXTRA_UI_TEXT, uiText);
            speak(ttsText, args);
        }
    }
}
