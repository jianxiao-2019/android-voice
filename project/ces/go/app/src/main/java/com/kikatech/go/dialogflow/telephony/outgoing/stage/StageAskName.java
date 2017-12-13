package com.kikatech.go.dialogflow.telephony.outgoing.stage;

import android.os.Bundle;

import com.kikatech.go.dialogflow.SceneUtil;
import com.kikatech.go.dialogflow.model.TtsText;
import com.kikatech.go.util.LogUtil;
import com.kikatech.voice.core.dialogflow.scene.ISceneFeedback;
import com.kikatech.voice.core.dialogflow.scene.SceneBase;
import com.kikatech.voice.core.dialogflow.scene.SceneStage;

/**
 * Created by tianli on 17-11-11.
 */

public class StageAskName extends StageOutgoing {
    private static final String TAG = "StageAskName";

    StageAskName(SceneBase scene, ISceneFeedback feedback) {
        super(scene, feedback);
    }

    @Override
    public SceneStage next(String action, Bundle extra) {
        return super.next(action, extra);
    }

    @Override
    public void doAction() {
        action();
    }

    @Override
    public void action() {
        String[] uiAndTtsText = SceneUtil.getAskContactToCall(mSceneBase.getContext());
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
