package com.kikatech.go.dialogflow.navigation.stage;

import android.os.Bundle;
import android.support.annotation.NonNull;

import com.kikatech.go.dialogflow.BaseSceneStage;
import com.kikatech.go.dialogflow.SceneUtil;
import com.kikatech.go.dialogflow.model.TtsText;
import com.kikatech.voice.core.dialogflow.scene.ISceneFeedback;
import com.kikatech.voice.core.dialogflow.scene.SceneBase;
import com.kikatech.voice.core.dialogflow.scene.SceneStage;

/**
 * @author SkeeterWang Created on 2017/11/27.
 */

public class StageCancelNaviation extends BaseSceneStage {

    public StageCancelNaviation(@NonNull SceneBase scene, ISceneFeedback feedback) {
        super(scene, feedback);
    }

    @Override
    public SceneStage next(String action, Bundle extra) {
        return null;
    }

    @Override
    public void prepare() {
    }

    @Override
    public void doAction() {
        action();
    }

    @Override
    public void action() {
        String[] uiAndTtsText = SceneUtil.getStopCommon(mSceneBase.getContext());
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
    public void onStageActionStart() {
    }

    @Override
    public void onStageActionDone(boolean isInterrupted) {
        exitScene();
    }
}
