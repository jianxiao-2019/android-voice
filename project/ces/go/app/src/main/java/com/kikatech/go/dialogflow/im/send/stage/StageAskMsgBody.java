package com.kikatech.go.dialogflow.im.send.stage;

import android.os.Bundle;
import android.support.annotation.NonNull;

import com.kikatech.go.dialogflow.im.IMContent;
import com.kikatech.go.util.LogUtil;
import com.kikatech.voice.core.dialogflow.intent.Intent;
import com.kikatech.voice.core.dialogflow.scene.ISceneFeedback;
import com.kikatech.voice.core.dialogflow.scene.SceneBase;
import com.kikatech.voice.core.dialogflow.scene.SceneStage;

/**
 * Created by brad_chang on 2017/11/23.
 */

public class StageAskMsgBody extends BaseSendIMStage {
    StageAskMsgBody(@NonNull SceneBase scene, ISceneFeedback feedback) {
        super(scene, feedback);
    }

    @Override
    protected SceneStage getNextStage(String action, Bundle extra) {
        setQueryAnyWords(false);
        String userSay = Intent.parseUserInput(extra);
        if(LogUtil.DEBUG) LogUtil.log(TAG, "userSay:" + userSay);
        IMContent imc = getIMContent();
        imc.updateMsgBody(userSay);
        return getCheckIMBodyStage(TAG, imc, mSceneBase, mFeedback);
    }

    @Override
    public void action() {
        setQueryAnyWords(true);
        speak("What is the message ?");
    }
}
