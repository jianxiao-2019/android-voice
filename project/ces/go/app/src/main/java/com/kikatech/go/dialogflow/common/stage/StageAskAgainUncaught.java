package com.kikatech.go.dialogflow.common.stage;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Pair;

import com.kikatech.go.dialogflow.SceneUtil;
import com.kikatech.go.dialogflow.common.SceneCommon;
import com.kikatech.go.dialogflow.model.TtsText;
import com.kikatech.go.util.LogUtil;
import com.kikatech.voice.core.dialogflow.intent.Intent;
import com.kikatech.voice.core.dialogflow.scene.ISceneFeedback;
import com.kikatech.voice.core.dialogflow.scene.SceneBase;
import com.kikatech.voice.core.dialogflow.scene.SceneStage;
import com.kikatech.voice.core.tts.TtsSource;

/**
 * @author SkeeterWang Created on 2017/12/13.
 */

public class StageAskAgainUncaught extends SceneStage {

    public StageAskAgainUncaught(@NonNull SceneBase scene, ISceneFeedback feedback) {
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
        if (LogUtil.DEBUG) {
            LogUtil.log(TAG, "isDefaultUncaught:" + isDefaultUncaught);
        }

        if(isDefaultUncaught) {
            exitScene();
        } else {
            String uiText = SceneCommon.PRE_UNCAUGHT;
            String ttsText = SceneUtil.getResponseNotGet(mSceneBase.getContext());

            Pair<String, Integer>[] pairs = new Pair[2];
            pairs[0] = new Pair<>(ttsText, TtsSource.TTS_SPEAKER_1);
            pairs[1] = new Pair<>(uiText, TtsSource.TTS_SPEAKER_1);

            TtsText tText = new TtsText(SceneUtil.ICON_COMMON, uiText);
            Bundle args = new Bundle();
            args.putParcelable(SceneUtil.EXTRA_TTS_TEXT, tText);

            speak(pairs, args);
        }
    }
}
