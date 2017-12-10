package com.kikatech.go.dialogflow.navigation.stage;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.kikatech.go.dialogflow.SceneUtil;
import com.kikatech.go.dialogflow.model.TtsText;
import com.kikatech.go.dialogflow.navigation.NaviSceneUtil;
import com.kikatech.go.navigation.google.webservice.GooglePlaceApi;
import com.kikatech.go.navigation.model.PlaceSearchResult;
import com.kikatech.go.util.LogUtil;
import com.kikatech.voice.core.dialogflow.intent.Intent;
import com.kikatech.voice.core.dialogflow.scene.ISceneFeedback;
import com.kikatech.voice.core.dialogflow.scene.SceneBase;
import com.kikatech.voice.core.dialogflow.scene.SceneStage;

/**
 * Created by bradchang on 2017/11/13.
 */

public class StageAskAddress extends BaseNaviStage {

    private final boolean mAgain;

    StageAskAddress(@NonNull SceneBase scene, ISceneFeedback feedback, boolean again) {
        super(scene, feedback);
        mAgain = again;
        if (LogUtil.DEBUG) LogUtil.log(TAG, "StageAskAddress init");
    }

    @Override
    public SceneStage next(String action, Bundle extra) {
        setQueryAnyWords(false);

        SceneStage superStage = super.next(action, extra);
        if (superStage != null) {
            return superStage;
        }

        String userSays = Intent.parseUserInput(extra);
        return new StageQueryAddress(mSceneBase, mFeedback, userSays);
    }

    @Override
    public void doAction() {
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