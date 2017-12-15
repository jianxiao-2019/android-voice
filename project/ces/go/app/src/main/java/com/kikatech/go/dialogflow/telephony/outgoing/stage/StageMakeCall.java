package com.kikatech.go.dialogflow.telephony.outgoing.stage;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;

import com.kikatech.go.dialogflow.SceneUtil;
import com.kikatech.go.dialogflow.model.TtsText;
import com.kikatech.go.telephony.TelephonyServiceManager;
import com.kikatech.go.util.BackgroundThread;
import com.kikatech.go.util.LogUtil;
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
    private String mNumberToCall;

    public StageMakeCall(SceneBase scene, ISceneFeedback feedback, ContactManager.PhoneBookContact contact) {
        super(scene, feedback);
        mContact = contact;
    }

    @Override
    public SceneStage next(String action, Bundle extra) {
        return null;
    }

    @Override
    public void doAction() {
        onStageActionStart();
        action();
    }

    @Override
    public void action() {
        String speech = "error occurred, please contact RD";
        String phoneNumber = null;
        if (mContact != null) {
            Context context = mSceneBase.getContext();
            String[] uiAndTtsText;
            if (!mContact.phoneNumbers.isEmpty()) {
                phoneNumber = mContact.phoneNumbers.get(0).number;
                if (!TextUtils.isEmpty(mContact.displayName)) {
                    uiAndTtsText = SceneUtil.getCallContact(context, mContact.displayName);
                } else {
                    uiAndTtsText = SceneUtil.getCallNumber(context);
                }
                if (uiAndTtsText.length > 0) {
                    String uiText = uiAndTtsText[0];
                    String ttsText = uiAndTtsText[1];
                    TtsText tText = new TtsText(SceneUtil.ICON_TELEPHONY, uiText);
                    Bundle args = new Bundle();
                    args.putParcelable(SceneUtil.EXTRA_TTS_TEXT, tText);
                    speech = ttsText;
                }
            }
        }
        if (LogUtil.DEBUG) {
            LogUtil.logv(TAG, speech);
        }
        mNumberToCall = phoneNumber;
        speak(speech);
    }

    @Override
    public void onStageActionDone(boolean isInterrupted, boolean delayAsrResume) {
        Bundle args = new Bundle();
        args.putString(SceneUtil.EXTRA_EVENT, SceneUtil.EVENT_OUTGOING_CALL);
        send(args);
        if (!TextUtils.isEmpty(mNumberToCall)) {
            TelephonyServiceManager.getIns().makePhoneCall(mSceneBase.getContext(), mNumberToCall);
        }
        BackgroundThread.getHandler().postDelayed(new Runnable() {
            @Override
            public void run() {
                exitScene();
            }
        }, SceneUtil.OUTGOING_CALL_PAGE_DELAY);
    }
}
