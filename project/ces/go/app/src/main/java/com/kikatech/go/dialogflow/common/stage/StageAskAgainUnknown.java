package com.kikatech.go.dialogflow.common.stage;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Pair;

import com.kikatech.go.dialogflow.SceneUtil;
import com.kikatech.go.dialogflow.common.SceneCommon;
import com.kikatech.go.dialogflow.model.TtsText;
import com.kikatech.voice.core.dialogflow.intent.Intent;
import com.kikatech.voice.core.dialogflow.scene.ISceneFeedback;
import com.kikatech.voice.core.dialogflow.scene.SceneBase;
import com.kikatech.voice.core.dialogflow.scene.SceneStage;
import com.kikatech.voice.core.tts.TtsSource;

/**
 * @author SkeeterWang Created on 2017/11/29.
 */

public class StageAskAgainUnknown extends SceneStage {

    public StageAskAgainUnknown(@NonNull SceneBase scene, ISceneFeedback feedback) {
        super(scene, feedback);
    }

    @Override
    public SceneStage next(String action, Bundle extra) {
        switch (action) {
            case Intent.ACTION_UNKNOWN:
            case Intent.ACTION_UNCAUGHT:
                return new StageStopSession(mSceneBase, mFeedback);
        }
        return null;
    }

    @Override
    protected void prepare() {
    }

    @Override
    public void doAction() {
        onStageActionStart();
        action();
    }

    @Override
    protected void action() {
        String uiText = SceneCommon.PRE_UNKNOWN;
        SceneUtil.UnknownIntentResult uir = SceneUtil.getRandomIntentUnknown(mSceneBase.getContext());
        TtsText tText = new TtsText(SceneUtil.ICON_COMMON, uiText);
        Bundle args = new Bundle();
        args.putParcelable(SceneUtil.EXTRA_TTS_TEXT, tText);

        Pair<String, Integer>[] pairs = new Pair[uir.appendCommonString ? 2 : 1];
        pairs[0] = new Pair<>(uir.response, TtsSource.TTS_SPEAKER_1);
        if (uir.appendCommonString) {
            pairs[1] = new Pair<>(SceneCommon.PRE_UNKNOWN, TtsSource.TTS_SPEAKER_1);
        }

        speak(pairs, args);
    }
}