package com.kikatech.go.dialogflow.sms.stage;

import android.support.annotation.NonNull;

import com.kikatech.voice.core.dialogflow.scene.ISceneFeedback;
import com.kikatech.voice.core.dialogflow.scene.SceneBase;

/**
 * Created by brad_chang on 2017/11/15.
 */

public class StageCancel extends BaseSendSmsStage {

    StageCancel(@NonNull SceneBase scene, ISceneFeedback feedback) {
        super(scene, feedback);
    }

    @Override
    public void action() {
        speak("Cancel Send SMS");
        exitScene();
    }
}
