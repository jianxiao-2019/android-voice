package com.kikatech.go.dialogflow.telephony.outgoing.stage;

import android.os.Bundle;
import android.support.annotation.NonNull;

import com.kikatech.go.util.LogUtil;
import com.kikatech.voice.core.dialogflow.scene.ISceneFeedback;
import com.kikatech.voice.core.dialogflow.scene.SceneBase;
import com.kikatech.voice.core.dialogflow.scene.SceneStage;

/**
 * Created by tianli on 17-11-11.
 */

public class StageNoContact extends StageOutgoing {
    private static final String TAG = "StageNoContact";

    public StageNoContact(@NonNull SceneBase scene, ISceneFeedback feedback) {
        super(scene, feedback);
    }

    @Override
    public SceneStage next(String action, Bundle extra) {
        return super.next(action, extra);
    }

    @Override
    public void action() {
        String speech = "Sorry, I couldn't find the contact. Please say again."; // doc 19
        if (LogUtil.DEBUG) {
            LogUtil.logv(TAG, speech);
        }
        speak(speech);
    }
}
