package com.kikatech.go.dialogflow.telephony.outgoing.stage;

import android.os.Bundle;

import com.kikatech.go.dialogflow.AsrConfigUtil;
import com.kikatech.go.dialogflow.SceneUtil;
import com.kikatech.go.dialogflow.model.TtsText;
import com.kikatech.go.util.LogUtil;
import com.kikatech.voice.core.dialogflow.intent.Intent;
import com.kikatech.voice.core.dialogflow.scene.ISceneFeedback;
import com.kikatech.voice.core.dialogflow.scene.SceneBase;
import com.kikatech.voice.core.dialogflow.scene.SceneStage;
import com.kikatech.voice.util.contact.ContactManager;

/**
 * Created by tianli on 17-11-11.
 */

public class StageAskName extends StageOutgoing {
    private static final String TAG = "StageAskName";

    StageAskName(SceneBase scene, ISceneFeedback feedback) {
        super(scene, feedback);
    }

    @Override
    protected @AsrConfigUtil.ASRMode int getAsrMode() {
        return AsrConfigUtil.ASR_MODE_SHORT_COMMAND_SPELLING;
    }

    @Override
    public SceneStage next(String action, Bundle extra) {
        setQueryAnyWords(false);
        if (LogUtil.DEBUG) LogUtil.log(TAG, "action:" + action + ", extra:" + extra);

        String[] nBestInput = Intent.parseUserInputNBest(extra);
        ContactManager.MatchedContact matchedContact = null;
        if (nBestInput != null && nBestInput.length != 0) {
            if (LogUtil.DEBUG) {
                LogUtil.log(TAG, "try parsing from asr n-best");
            }
            matchedContact = ContactManager.getIns().findContact(mSceneBase.getContext(), nBestInput);
            if (LogUtil.DEBUG) {
                LogUtil.log(TAG, "matchedContact:" + matchedContact);
            }
        }
        StageOutgoing nextStage = getMatchedContactStage(matchedContact);
        if (nextStage != null) {
            return nextStage;
        } else {
            if (LogUtil.DEBUG) {
                LogUtil.log(TAG, "Cannot find matched contact, ask the calling target again");
            }
            return new StageAskName(mSceneBase, mFeedback);
        }
    }

    @Override
    public void doAction() {
        onStageActionStart();
        action();
    }

    @Override
    public void action() {
        setQueryAnyWords(true);
        String[] uiAndTtsText = SceneUtil.getAskContactToCall(mSceneBase.getContext());
        if (uiAndTtsText.length > 0) {
            String uiText = uiAndTtsText[0];
            String ttsText = uiAndTtsText[1];
            TtsText tText = new TtsText(SceneUtil.ICON_TELEPHONY, uiText);
            Bundle args = new Bundle();
            args.putParcelable(SceneUtil.EXTRA_TTS_TEXT, tText);
            speak(ttsText, args);
        }
    }
}