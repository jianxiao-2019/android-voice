package com.kikatech.go.dialogflow;

import android.content.Context;
import android.os.Bundle;
import android.util.Pair;

import com.kikatech.go.dialogflow.model.TtsText;
import com.kikatech.voice.core.dialogflow.scene.ISceneFeedback;
import com.kikatech.voice.core.dialogflow.scene.SceneBase;
import com.kikatech.voice.core.dialogflow.scene.SceneStage;
import com.kikatech.voice.core.tts.TtsSource;

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
                onStageActionStart();
                action();
            }

            @Override
            protected void prepare() {
            }

            @Override
            protected void action() {
                String[] stopCommonUiAndTts = SceneUtil.getStopCommon(mSceneBase.getContext());
                String uiText = stopCommonUiAndTts[0];
                String ttsText = SceneUtil.getIntentUnknown(mSceneBase.getContext());
                TtsText tText = new TtsText(SceneUtil.ICON_COMMON, uiText);
                Bundle args = new Bundle();
                args.putParcelable(SceneUtil.EXTRA_TTS_TEXT, tText);

                Pair<String, Integer>[] pairs = new Pair[2];
                pairs[0] = new Pair<>(ttsText, TtsSource.TTS_SPEAKER_1);
                pairs[1] = new Pair<>(stopCommonUiAndTts[1], TtsSource.TTS_SPEAKER_1);

                speak(pairs, args);
            }

            @Override
            public void onStageActionDone(boolean isInterrupted) {
                exitScene();
            }
        };
    }
}
