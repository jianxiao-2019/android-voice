package com.kikatech.go.dialogflow.stop.stage;

import android.os.Bundle;
import android.support.annotation.NonNull;

import com.kikatech.go.dialogflow.BaseSceneStage;
import com.kikatech.go.dialogflow.SceneUtil;
import com.kikatech.go.dialogflow.model.TtsText;
import com.kikatech.go.util.StringUtil;
import com.kikatech.voice.core.dialogflow.scene.ISceneFeedback;
import com.kikatech.voice.core.dialogflow.scene.SceneBase;
import com.kikatech.voice.core.dialogflow.scene.SceneStage;

/**
 * @author SkeeterWang Created on 2017/11/30.
 */

public class StageStop extends BaseSceneStage {
    private String mStopKeyWord;

    public StageStop(@NonNull SceneBase scene, ISceneFeedback feedback, String stopKeyWord) {
        super(scene, feedback);
        mStopKeyWord = stopKeyWord;
    }

    @Override
    public SceneStage next(String action, Bundle extra) {
        return null;
    }

    @Override
    protected void prepare() {
        Bundle args = new Bundle();
        args.putString(SceneUtil.EXTRA_EVENT, SceneUtil.EVENT_SYNONYM_RESULT);
        args.putString(SceneUtil.EXTRA_UI_TEXT, StringUtil.upperCaseFirstWord(mStopKeyWord));
        send(args);
    }

    @Override
    public void doAction() {
        onStageActionStart();
        action();
    }

    @Override
    protected void action() {
        String[] uiAndTtsText = SceneUtil.getStopCommon(mSceneBase.getContext());
        if (uiAndTtsText.length > 0) {
            String uiText = uiAndTtsText[0];
            String ttsText = uiAndTtsText[1];
            TtsText tText = new TtsText(SceneUtil.ICON_COMMON, uiText);
            Bundle args = new Bundle();
            args.putParcelable(SceneUtil.EXTRA_TTS_TEXT, tText);
            speak(ttsText, args);
        }
    }

    @Override
    public void onStageActionDone(boolean isInterrupted) {
        exitScene();
    }
}