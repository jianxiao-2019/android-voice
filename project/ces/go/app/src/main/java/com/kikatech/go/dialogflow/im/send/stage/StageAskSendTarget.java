package com.kikatech.go.dialogflow.im.send.stage;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.kikatech.go.dialogflow.im.IMContent;
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
        if (TextUtils.isEmpty(imc.getSendTarget())) {
            String userSay = extra.getString(Intent.KEY_USER_INPUT, "");
            if (!TextUtils.isEmpty(userSay)) {
                imc.updateSendTarget(userSay);
            }
        }
        return getCheckSendTargetStage(TAG, getIMContent(), mSceneBase, mFeedback);
    }

    @Override
    public void action() {
        setQueryAnyWords(true);
        speak("Who do you want to send ?");
    }
}
