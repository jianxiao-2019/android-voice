package com.kikatech.go.tutorial.dialogflow.stage;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.kikatech.go.tutorial.TutorialUtil;
import com.kikatech.go.util.LogUtil;
import com.kikatech.voice.core.dialogflow.intent.Intent;
import com.kikatech.voice.core.dialogflow.scene.ISceneFeedback;
import com.kikatech.voice.core.dialogflow.scene.SceneBase;
import com.kikatech.voice.core.dialogflow.scene.SceneStage;

/**
 * @author SkeeterWang Created on 2018/5/11.
 */

class TStageNavAskCommand extends BaseTutorialStage {
    protected final String TAG = "TStageNavAskCommand";

    // 1-2-3
    TStageNavAskCommand(@NonNull SceneBase scene, ISceneFeedback feedback) {
        super(scene, feedback);
    }

    @Override
    public SceneStage next(String action, Bundle extra) {
        return super.next(action, extra);
    }

    @Override
    SceneStage getBackStage() {
        return new TStageNavAskCommandAlert(mSceneBase, mFeedback);
    }

    @Override
    SceneStage getNexStage(@NonNull String action, Bundle extra) {
        switch (action) {
            case Intent.ACTION_USER_INPUT:
                String userInput = Intent.parseUserInput(extra);
                if (!TextUtils.isEmpty(userInput) && "navigate".equals(userInput.toLowerCase())) {
                    // goto 1-2-4
                    return new TStageNavAskAddressAlert(mSceneBase, mFeedback);
                } else {
                    return getBackStage();
                }
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
    protected void action() {
        if (LogUtil.DEBUG) {
            LogUtil.logd(TAG, "action");
        }
        setQueryAnyWords(true);
        Bundle extras = new Bundle();
        extras.putInt(TutorialUtil.StageEvent.KEY_TYPE, TutorialUtil.StageActionType.ASR);
        send(extras);
        // start asr, except "Navigate"
    }


}
