package com.kikatech.go.dialogflow.telephony.outgoing.stage;

import android.os.Bundle;
import android.support.annotation.NonNull;

import com.kikatech.go.dialogflow.SceneUtil;
import com.kikatech.go.dialogflow.model.TtsText;
import com.kikatech.voice.core.dialogflow.scene.ISceneFeedback;
import com.kikatech.voice.core.dialogflow.scene.SceneBase;

/**
 * Created by tianli on 17-11-11.
 */

public class StageNoContact extends StageOutgoing {

    StageNoContact(@NonNull SceneBase scene, ISceneFeedback feedback) {
        super(scene, feedback);
    }

    @Override
    public void doAction() {
        onStageActionStart();
        action();
    }

    @Override
    public void action() {
        String[] uiAndTtsText = SceneUtil.getContactNotFound(mSceneBase.getContext());
        if (uiAndTtsText.length > 0) {
            String uiText = uiAndTtsText[0];
            String ttsText = uiAndTtsText[1];
            TtsText tText = new TtsText(SceneUtil.ICON_TELEPHONY, uiText);
            Bundle args = new Bundle();
            args.putParcelable(SceneUtil.EXTRA_TTS_TEXT, tText);
            speak(ttsText, args);
        }
    }
}
