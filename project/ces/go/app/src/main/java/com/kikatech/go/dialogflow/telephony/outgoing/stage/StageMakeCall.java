package com.kikatech.go.dialogflow.telephony.outgoing.stage;

import android.os.Bundle;
import android.text.TextUtils;

import com.kikatech.go.telephony.TelephonyServiceManager;
import com.kikatech.go.util.LogUtil;
import com.kikatech.voice.core.dialogflow.scene.IDialogFlowFeedback;
import com.kikatech.voice.core.dialogflow.scene.ISceneFeedback;
import com.kikatech.voice.core.dialogflow.scene.SceneBase;
import com.kikatech.voice.core.dialogflow.scene.SceneStage;
import com.kikatech.voice.util.contact.ContactManager;

/**
 * @author SkeeterWang Created on 2017/11/13.
 */

public class StageMakeCall extends StageOutgoing {

    private static final String TAG = "StageMakeCall";

    private ContactManager.PhoneBookContact mContact;

    public StageMakeCall(SceneBase scene, ISceneFeedback feedback, ContactManager.PhoneBookContact contact) {
        super(scene, feedback);
        mContact = contact;
    }

    @Override
    public SceneStage next(String action, Bundle extra) {
        return null;
    }

    @Override
    public void action() {
        String speech = "error occurred, please contact RD";
        String phoneNumber = null;
        if (mContact != null) {
            if (mContact.phoneNumbers != null && !mContact.phoneNumbers.isEmpty()) {
                phoneNumber = mContact.phoneNumbers.get(0);
                if (!TextUtils.isEmpty(mContact.displayName)) {
                    speech = String.format("ok, make a call to %1$s, dial number %2$s", mContact.displayName, phoneNumber);
                } else {
                    speech = String.format("ok, make a call, dial number %1$s", phoneNumber);
                }
            }
        }
        if (LogUtil.DEBUG) {
            LogUtil.logv(TAG, speech);
        }
        final String finalPhoneNumber = phoneNumber;
        speak(speech, new IDialogFlowFeedback.IToSceneFeedback() {
            @Override
            public void onTtsStart() {
            }

            @Override
            public void onTtsComplete() {
                makePhoneCall(finalPhoneNumber);
            }

            @Override
            public void onTtsError() {
                makePhoneCall(finalPhoneNumber);
            }

            @Override
            public void onTtsInterrupted() {
                makePhoneCall(finalPhoneNumber);
            }

            @Override
            public boolean isEndOfScene() {
                return true;
            }
        });
    }

    private void makePhoneCall(String number) {
        if (!TextUtils.isEmpty(number)) {
            TelephonyServiceManager.getIns().makePhoneCall(mSceneBase.getContext(), number);
            exitScene();
        }
    }
}
