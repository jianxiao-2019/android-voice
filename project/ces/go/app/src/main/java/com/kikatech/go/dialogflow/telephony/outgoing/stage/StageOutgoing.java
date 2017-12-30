package com.kikatech.go.dialogflow.telephony.outgoing.stage;

import android.os.Bundle;
import android.text.TextUtils;

import com.kikatech.go.dialogflow.AsrConfigUtil;
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

    public StageOutgoing(SceneBase scene, ISceneFeedback feedback) {
        super(scene, feedback);
    }

    @Override
    @AsrConfigUtil.ASRMode
    protected int getAsrMode() {
        return AsrConfigUtil.ASR_MODE_SHORT_COMMAND;
    }

    @Override
    public SceneStage next(String action, Bundle extra) {
        if (!TextUtils.isEmpty(action)) {
            switch (action) {
//                case Intent.ACTION_USER_INPUT:
                case SceneActions.ACTION_OUTGOING_NUMBERS:
                case SceneActions.ACTION_OUTGOING_START:
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
        if (LogUtil.DEBUG) {
            LogUtil.log(TAG, "getCheckContactStage: action: " + action);
        }
        boolean hasQueried = false;
        ContactManager.MatchedContact matchedContact = null;

        // try parsing from api.ai result
        String targetName = extra != null ? extra.getString(SceneActions.PARAM_OUTGOING_NAME) : null;
        try {
            targetName = targetName.substring(1, targetName.length() - 1);
        } catch (Exception ignore) {
        }
        if (!TextUtils.isEmpty(targetName)) {
            if (LogUtil.DEBUG) {
                LogUtil.logd(TAG, "try parsing from api.ai result");
            }
            matchedContact = ContactManager.getIns().findContact(mSceneBase.getContext(), targetName);
            hasQueried = true;
        }

        // try parsing from asr n-best
        if (matchedContact == null && !TextUtils.isEmpty(targetName)) {
            String[] nBestInput = extra != null ? Intent.parseUserInputNBest(extra) : null;
            if (nBestInput != null && nBestInput.length != 0) {
                if (LogUtil.DEBUG) {
                    LogUtil.logd(TAG, "try parsing from asr n-best");
                }
                matchedContact = ContactManager.getIns().findContact(mSceneBase.getContext(), nBestInput);
                hasQueried = true;
            }
        }

        // check query result
        StageOutgoing next = getMatchedContactStage(matchedContact);
        if (next != null) {
            return next;
        } else {
            return hasQueried ? new StageNoContact(mSceneBase, mFeedback) : new StageAskName(mSceneBase, mFeedback);
        }
    }

    StageOutgoing getMatchedContactStage(ContactManager.MatchedContact matchedContact) {
        if (matchedContact != null) {
            switch (matchedContact.matchedType) {
                case ContactManager.MatchedContact.MatchedType.FULL_MATCHED:
                    if (matchedContact.phoneNumbers.size() > 1) {
                        return new StageConfirmNumber(mSceneBase, mFeedback, matchedContact);
                    } else {
                        return new StageMakeCall(mSceneBase, mFeedback, matchedContact);
                    }
                case ContactManager.MatchedContact.MatchedType.NUMBER_MATCHED:
                    return new StageMakeCall(mSceneBase, mFeedback, matchedContact);
                case ContactManager.MatchedContact.MatchedType.FUZZY_MATCHED:
                    return new StageConfirmName(mSceneBase, mFeedback, matchedContact);
                default:
                    return new StageNoContact(mSceneBase, mFeedback);
            }
        }
        return null;
    }
}
