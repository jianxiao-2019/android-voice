package com.kikatech.go.dialogflow.music.stage;

import android.os.Bundle;
import android.support.annotation.NonNull;

import com.kikatech.go.dialogflow.SceneUtil;
import com.kikatech.go.dialogflow.UserSettings;
import com.kikatech.go.dialogflow.model.TtsText;
import com.kikatech.go.util.LogUtil;
import com.kikatech.voice.core.dialogflow.intent.Intent;
import com.kikatech.voice.core.dialogflow.scene.ISceneFeedback;
import com.kikatech.voice.core.dialogflow.scene.SceneBase;
import com.kikatech.voice.core.dialogflow.scene.SceneStage;
import com.kikatech.voice.service.conf.AsrConfiguration;

/**
 * @author SkeeterWang Created on 2018/3/22.
 */

public class StageAskSong extends BaseMusicStage {

    StageAskSong(@NonNull SceneBase scene, ISceneFeedback feedback) {
        super(scene, feedback);
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
        String userSay = Intent.parseUserInput(extra);
        if (LogUtil.DEBUG) {
            LogUtil.log(TAG, "userSay:" + userSay);
        }
        return new StageQuerySong(mSceneBase, mFeedback, userSay);
    }

    @Override
    public void doAction() {
        onStageActionStart();
        action();
    }

    @Override
    public void action() {
        setQueryAnyWords(true);
        String[] uiAndTtsText = SceneUtil.getAskSong(mSceneBase.getContext());
        if (uiAndTtsText.length > 0) {
            String uiText = uiAndTtsText[0];
            String ttsText = uiAndTtsText[1];
            TtsText tText = new TtsText(SceneUtil.ICON_MUSIC, uiText);
            Bundle args = new Bundle();
            args.putParcelable(SceneUtil.EXTRA_TTS_TEXT, tText);
            speak(ttsText, args);
        }
    }
}
