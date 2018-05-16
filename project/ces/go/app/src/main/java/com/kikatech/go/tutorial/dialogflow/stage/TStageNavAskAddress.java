package com.kikatech.go.tutorial.dialogflow.stage;

import android.os.Bundle;
import android.support.annotation.NonNull;

import com.kikatech.go.dialogflow.SceneUtil;
import com.kikatech.go.dialogflow.model.TtsText;
import com.kikatech.go.tutorial.TutorialUtil;
import com.kikatech.go.util.LogUtil;
import com.kikatech.voice.core.dialogflow.intent.Intent;
import com.kikatech.voice.core.dialogflow.scene.ISceneFeedback;
import com.kikatech.voice.core.dialogflow.scene.SceneBase;
import com.kikatech.voice.core.dialogflow.scene.SceneStage;

/**
 * @author SkeeterWang Created on 2018/5/11.
 */

class TStageNavAskAddress extends BaseTutorialStage {
    protected final String TAG = "TStageNavAskAddress";

    // 1-3-1
    TStageNavAskAddress(@NonNull SceneBase scene, ISceneFeedback feedback) {
        super(scene, feedback);
    }

    @Override
    public SceneStage next(String action, Bundle extra) {
        return super.next(action, extra);
    }

    @Override
    SceneStage getBackStage() {
        return new TStageNavAskAddressAlert(mSceneBase, mFeedback);
    }

    @Override
    SceneStage getNexStage(@NonNull String action, Bundle extra) {
        switch (action) {
            case Intent.ACTION_USER_INPUT:
                String mAddress = Intent.parseUserInput(extra);
                // goto 1-3-3
                return new TStageNavAskConfirmAlert(mSceneBase, mFeedback, mAddress);
        }
        return super.onInputIncorrect();
    }

    @Override
    protected void prepare() {
        if (LogUtil.DEBUG) {
            LogUtil.logd(TAG, "prepare");
        }
    }

    @Override
    public void doAction() {
        onStageActionStart();
        action();
    }

    @Override
    protected void action() {
        if (LogUtil.DEBUG) {
            LogUtil.logd(TAG, "action");
        }
        setQueryAnyWords(true);
        String[] uiAndTtsText = SceneUtil.getAskAddress(mSceneBase.getContext());
        if (uiAndTtsText.length > 0) {
            String uiText = uiAndTtsText[0];
            String ttsText = uiAndTtsText[1];
            TtsText tText = new TtsText(SceneUtil.ICON_NAVIGATION, uiText);
            Bundle args = new Bundle();
            args.putParcelable(SceneUtil.EXTRA_TTS_TEXT, tText);
            speak(ttsText, args);
        }
    }

    @Override
    public void onStageActionDone(boolean isInterrupted) {
        super.onStageActionDone(isInterrupted);
        // start asr, except {address}
        Bundle extras = new Bundle();
        extras.putInt(TutorialUtil.StageEvent.KEY_TYPE, TutorialUtil.StageActionType.ASR);
        send(extras);
    }
}
