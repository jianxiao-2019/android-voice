package com.kikatech.go.dialogflow.im.send.stage;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.kikatech.go.dialogflow.SceneUtil;
import com.kikatech.go.dialogflow.im.send.IMContent;
import com.kikatech.voice.core.dialogflow.intent.Intent;
import com.kikatech.voice.core.dialogflow.scene.ISceneFeedback;
import com.kikatech.voice.core.dialogflow.scene.SceneBase;
import com.kikatech.voice.core.dialogflow.scene.SceneStage;

/**
 * Created by brad_chang on 2017/11/23.
 */

public class StageAskSendTarget extends BaseSendIMStage {
    StageAskSendTarget(@NonNull SceneBase scene, ISceneFeedback feedback) {
        super(scene, feedback);
    }

    @Override
    protected SceneStage getNextStage(String action, Bundle extra) {
        setQueryAnyWords(false);
        IMContent imc = getIMContent();
        String userSay = Intent.parseUserInput(extra);
        if (!TextUtils.isEmpty(userSay)) {
            imc.updateSendTarget(userSay);
        }
        return getCheckSendTargetStage(TAG, getIMContent(), mSceneBase, mFeedback);
    }

    @Override
    public void doAction() {
        action();
    }

    @Override
    public void action() {
        setQueryAnyWords(true);
        String[] uiAndTtsText = SceneUtil.getAskContact(mSceneBase.getContext());
        if (uiAndTtsText.length > 0) {
            String uiText = uiAndTtsText[0];
            String ttsText = uiAndTtsText[1];
            Bundle args = new Bundle();
            args.putString(SceneUtil.EXTRA_UI_TEXT, uiText);
            speak(ttsText, args);
        }
    }
}
