package com.kikatech.go.dialogflow;

import android.content.Context;
import android.os.Bundle;

import com.kikatech.voice.core.dialogflow.scene.ISceneFeedback;
import com.kikatech.voice.core.dialogflow.scene.SceneBase;
import com.kikatech.voice.core.dialogflow.scene.SceneStage;

/**
 * @author SkeeterWang Created on 2017/11/30.
 */

public abstract class NonLoopSceneBase extends SceneBase {

    public NonLoopSceneBase(Context context, ISceneFeedback feedback) {
        super(context, feedback);
    }

    @Override
    protected SceneStage onOverCounts() {
        return new SceneStage(this, mFeedback) {
            @Override
            public SceneStage next(String action, Bundle extra) {
                return null;
            }

            @Override
            public void doAction() {
                action();
            }

            @Override
            protected void prepare() {
            }

            @Override
            protected void action() {
                String[] stopCommonUiAndTts = SceneUtil.getStopCommon(mSceneBase.getContext());
                String uiText = stopCommonUiAndTts[0];
                String ttsText = SceneUtil.getIntentUnknown(mSceneBase.getContext(), stopCommonUiAndTts[1]);
                Bundle args = new Bundle();
                args.putString(SceneUtil.EXTRA_UI_TEXT, uiText);
                speak(ttsText, args);
            }

            @Override
            public void onStageActionDone(boolean isInterrupted) {
                exitScene();
            }
        };
    }
}