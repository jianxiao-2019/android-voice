package com.kikatech.go.dialogflow.telephony.outgoing.stage;

import android.os.Bundle;

import com.kikatech.go.util.LogUtil;
import com.kikatech.voice.core.dialogflow.scene.ISceneFeedback;
import com.kikatech.voice.core.dialogflow.scene.SceneBase;
import com.kikatech.voice.core.dialogflow.scene.SceneStage;

/**
 * Created by tianli on 17-11-11.
 */

public class StageAskName extends StageOutgoing {
    private static final String TAG = "StageAskName";

    public StageAskName(SceneBase scene, ISceneFeedback feedback) {
        super(scene, feedback);
    }

    @Override
    public SceneStage next(String action, Bundle extra) {
        return super.next(action, extra);
    }

    @Override
    public void action() {
        String speech = "Who do you want to call?";
        if (LogUtil.DEBUG) {
            LogUtil.logv(TAG, speech);
        }
        speak(speech);
    }
}
