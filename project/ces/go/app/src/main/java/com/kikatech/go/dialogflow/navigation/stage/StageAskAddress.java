package com.kikatech.go.dialogflow.navigation.stage;

import android.os.Bundle;
import android.support.annotation.NonNull;

import com.kikatech.go.dialogflow.AsrConfigUtil;
import com.kikatech.go.dialogflow.SceneUtil;
import com.kikatech.go.dialogflow.UserSettings;
import com.kikatech.go.dialogflow.model.TtsText;
import com.kikatech.voice.core.dialogflow.intent.Intent;
import com.kikatech.voice.core.dialogflow.scene.ISceneFeedback;
import com.kikatech.voice.core.dialogflow.scene.SceneBase;
import com.kikatech.voice.core.dialogflow.scene.SceneStage;
import com.kikatech.voice.service.conf.AsrConfiguration;

/**
 * Created by bradchang on 2017/11/13.
 */

public class StageAskAddress extends BaseNaviStage {

    private final boolean mAgain;

    StageAskAddress(@NonNull SceneBase scene, ISceneFeedback feedback, boolean again) {
        super(scene, feedback);
        mAgain = again;
    }

    @Override
    @AsrConfigUtil.ASRMode
    protected int getAsrMode() {
        return AsrConfigUtil.ASR_MODE_CONVERSATION_COMMAND;
    }

    @Override
    protected String getAsrLocale() {
        switch (UserSettings.getSettingAsrLocale()) {
            case UserSettings.AsrLocale.ZH:
                return AsrConfiguration.SupportedLanguage.ZH_TW;
            default:
            case UserSettings.AsrLocale.EN:
                return AsrConfiguration.SupportedLanguage.EN_US;
        }
    }

    @Override
    public SceneStage next(String action, Bundle extra) {
        setQueryAnyWords(false);

        SceneStage superStage = super.next(action, extra);
        if (superStage != null) {
            return superStage;
        }

        String userSays = Intent.parseUserInput(extra);
        String[] userInputs = Intent.parseUserInputNBest(extra);
        SceneStage stageGo = getStageByCheckDestination(userInputs);
        return stageGo != null ? stageGo : new StageConfirmAddress(mSceneBase, mFeedback, userSays, userSays);
    }

    @Override
    public void doAction() {
        onStageActionStart();
        action();
    }

    @Override
    public void action() {
        setQueryAnyWords(true);
        String[] uiAndTtsText;
        if (mAgain) {
            uiAndTtsText = SceneUtil.getAskAddressAgain(mSceneBase.getContext());
        } else {
            uiAndTtsText = SceneUtil.getAskAddress(mSceneBase.getContext());
        }
        if (uiAndTtsText.length > 0) {
            String uiText = uiAndTtsText[0];
            String ttsText = uiAndTtsText[1];
            TtsText tText = new TtsText(SceneUtil.ICON_NAVIGATION, uiText);
            Bundle args = new Bundle();
            args.putParcelable(SceneUtil.EXTRA_TTS_TEXT, tText);
            speak(ttsText, args);
        }
    }
}