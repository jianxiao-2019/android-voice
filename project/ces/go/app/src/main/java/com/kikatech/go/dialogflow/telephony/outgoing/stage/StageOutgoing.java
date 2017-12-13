package com.kikatech.go.dialogflow.telephony.outgoing.stage;

import android.os.Bundle;
import android.text.TextUtils;

import com.kikatech.go.dialogflow.BaseSceneStage;
import com.kikatech.go.dialogflow.telephony.outgoing.SceneActions;
import com.kikatech.go.util.LogUtil;
import com.kikatech.voice.core.dialogflow.intent.Intent;
import com.kikatech.voice.core.dialogflow.scene.ISceneFeedback;
import com.kikatech.voice.core.dialogflow.scene.SceneBase;
import com.kikatech.voice.core.dialogflow.scene.SceneStage;
import com.kikatech.voice.util.contact.ContactManager;

/**
 * Created by tianli on 17-11-11.
 */

public class StageOutgoing extends BaseSceneStage {
    private static final String TAG = "StageOutgoing";

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
                case SceneActions.ACTION_OUTGOING_YES:
                    return getCheckContactStage(action, extra);
                case SceneActions.ACTION_OUTGOING_NO:
                case SceneActions.ACTION_OUTGOING_CANCEL:
                    return new StageCancel(mSceneBase, mFeedback);
            }
        }
        return null;
    }

    @Override
    public void prepare() {
    }

    @Override
    public void action() {
    }

    private SceneStage getCheckContactStage(String action, Bundle extra) {
        boolean isStartStage = SceneActions.ACTION_OUTGOING_START.equals(action);
        boolean hasQueried = false;
        ContactManager.MatchedContact mMatchedContact = null;

        // try parsing from api.ai result
        String targetName = extra != null ? extra.getString(SceneActions.PARAM_OUTGOING_NAME) : null;
        try {
            targetName = targetName.substring(1, targetName.length() - 1);
        } catch (Exception ignore) {
        }
        if (!TextUtils.isEmpty(targetName)) {
            if (LogUtil.DEBUG) {
                LogUtil.log(TAG, "try parsing from api.ai result");
            }
            mMatchedContact = ContactManager.getIns().findContact(mSceneBase.getContext(), targetName);
            hasQueried = true;
        }

        // try parsing from asr n-best
        if (mMatchedContact == null) {
            String[] nBestInput = extra != null ? Intent.parseUserInputNBest(extra) : null;
            if (nBestInput != null && nBestInput.length != 0) {
                if (LogUtil.DEBUG) {
                    LogUtil.log(TAG, "try parsing from asr n-best");
                }
                mMatchedContact = ContactManager.getIns().findContact(mSceneBase.getContext(), nBestInput);
                hasQueried = true;
            }
        }

        // check query result
        if (mMatchedContact != null) {
            switch (mMatchedContact.matchedType) {
                case ContactManager.MatchedContact.MatchedType.FULL_MATCHED:
                    if (mMatchedContact.phoneNumbers.size() > 1) {
                        return new StageConfirmNumber(mSceneBase, mFeedback, mMatchedContact);
                    } else {
                        return new StageMakeCall(mSceneBase, mFeedback, mMatchedContact);
                    }
                case ContactManager.MatchedContact.MatchedType.NUMBER_MATCHED:
                    return new StageMakeCall(mSceneBase, mFeedback, mMatchedContact);
                case ContactManager.MatchedContact.MatchedType.FUZZY_MATCHED:
                    return new StageConfirmName(mSceneBase, mFeedback, mMatchedContact);
                default:
                    return new StageNoContact(mSceneBase, mFeedback);
            }
        } else {
            return !isStartStage && hasQueried ? new StageNoContact(mSceneBase, mFeedback) : new StageAskName(mSceneBase, mFeedback);
        }
    }
}
