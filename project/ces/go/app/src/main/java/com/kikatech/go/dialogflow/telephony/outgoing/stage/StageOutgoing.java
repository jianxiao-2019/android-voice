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
 * Created by tianli on 17-11-11.
 */

public class StageOutgoing extends SceneStage {
    private static final String TAG = "StageOutgoing";

    private static final byte NAME_STATE_NULL = 0x01;
    private static final byte NAME_STATE_FULL_MACH = 0x02;
    private static final byte NAME_STATE_FUZZY_MATCH = 0x03;
    private static final byte NAME_STATE_NUMBER_MATCH = 0x04;
    private static final byte NAME_STATE_NOT_FOUND = 0x05;

    private String mTargetName;
    private ContactManager.PhoneBookContact mContact;

    public StageOutgoing(SceneBase scene, ISceneFeedback feedback) {
        super(scene, feedback);
    }

    @Override
    public SceneStage next(String action, Bundle extra) {
        if (!TextUtils.isEmpty(action)) {
            switch (action) {
                case SceneActions.ACTION_OUTGOING_NUMBERS:
                case SceneActions.ACTION_OUTGOING_START:
                case SceneActions.ACTION_OUTGOING_CHANGE:
                    if (extra != null && extra.containsKey(SceneActions.PARAM_OUTGOING_NAME)) {
                        mTargetName = extra.getString(SceneActions.PARAM_OUTGOING_NAME);
                    }
                    int contactState = queryContact();
                    switch (contactState) {
                        case NAME_STATE_FULL_MACH:
                            if (mContact.phoneNumbers.size() > 1) {
                                return new StageConfirmNumber(mSceneBase, mFeedback, mContact);
                            } else {
                                return new StageMakeCall(mSceneBase, mFeedback, mContact);
                            }
                        case NAME_STATE_NUMBER_MATCH:
                            return new StageMakeCall(mSceneBase, mFeedback, mContact);
                        case NAME_STATE_NOT_FOUND:
                            return new StageNoContact(mSceneBase, mFeedback);
                        case NAME_STATE_FUZZY_MATCH:
                            return new StageConfirmName(mSceneBase, mFeedback, mContact);
                        default:
                        case NAME_STATE_NULL:
                            return new StageAskName(mSceneBase, mFeedback);
                    }
                case SceneActions.ACTION_OUTGOING_CANCEL:
                    return new StageCancel(mSceneBase, mFeedback);
            }
        }
        return null;
    }

    @Override
    public void action() {
    }

    private int queryContact() {
        if (LogUtil.DEBUG) LogUtil.log(TAG, "target name: " + mTargetName);
        if (!TextUtils.isEmpty(mTargetName)) {
            try {
                mTargetName = mTargetName.substring(1, mTargetName.length() - 1);
            } catch (Exception ignore) {
            }
            mContact = ContactManager.getIns().findName(mSceneBase.getContext(), mTargetName);
            if (mContact != null) {
                if (LogUtil.DEBUG) {
                    LogUtil.log(TAG, "name: " + mTargetName + ", displayName: " + mContact.displayName);
                }

                if (!TextUtils.isEmpty(mContact.displayName)) {
                    if (mTargetName.equals(mContact.displayName)) {
                        return NAME_STATE_FULL_MACH;
                    } else {
                        return NAME_STATE_FUZZY_MATCH;
                    }
                } else if (mContact.phoneNumbers != null && !mContact.phoneNumbers.isEmpty()) {
                    return NAME_STATE_NUMBER_MATCH;
                }
            } else {
                return NAME_STATE_NOT_FOUND;
            }
        }
        return NAME_STATE_NULL;
    }
}
