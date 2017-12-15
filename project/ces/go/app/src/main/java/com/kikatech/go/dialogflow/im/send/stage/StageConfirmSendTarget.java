package com.kikatech.go.dialogflow.im.send.stage;

import android.os.Bundle;
import android.support.annotation.NonNull;

import com.kikatech.go.dialogflow.AsrConfigUtil;
import com.kikatech.go.dialogflow.SceneUtil;
import com.kikatech.go.dialogflow.im.send.IMContent;
import com.kikatech.go.dialogflow.im.send.SceneActions;
import com.kikatech.go.dialogflow.model.TtsText;
import com.kikatech.voice.core.dialogflow.intent.Intent;
import com.kikatech.voice.core.dialogflow.scene.ISceneFeedback;
import com.kikatech.voice.core.dialogflow.scene.SceneBase;
import com.kikatech.voice.core.dialogflow.scene.SceneStage;

/**
 * Created by brad_chang on 2017/11/23.
 */

public class StageConfirmSendTarget extends BaseSendIMStage {
    StageConfirmSendTarget(@NonNull SceneBase scene, ISceneFeedback feedback) {
        super(scene, feedback);
    }

    @Override
    protected @AsrConfigUtil.ASRMode int getAsrMode() {
        return AsrConfigUtil.ASR_MODE_CONVERSATION_CMD_ALTER;
    }

    @Override
    protected SceneStage getNextStage(String action, Bundle extra) {
        IMContent imc = getIMContent();
        // Check yes or no
        switch (action) {
            case SceneActions.ACTION_SEND_IM_YES:
                imc.userConfirmSendTarget();
                return getCheckIMBodyStage(TAG, imc, mSceneBase, mFeedback);
            case SceneActions.ACTION_SEND_IM_NO:
                imc.updateSendTarget(null);
                return getCheckSendTargetStage(TAG, imc, mSceneBase, mFeedback);
            default:
                String[] userSay = Intent.parseUserInputNBest(extra);
                if (userSay != null && userSay.length != 0) {
                    imc.updateSendTarget(userSay);
                }
                return getCheckSendTargetStage(TAG, imc, mSceneBase, mFeedback);
        }
    }

    @Override
    public void doAction() {
        onStageActionStart();
        action();
    }

    @Override
    public void action() {
        IMContent imc = getIMContent();
        String[] uiAndTtsText = SceneUtil.getConfirmContact(mSceneBase.getContext(), imc.getSendTarget());
        if (uiAndTtsText.length > 0) {
            String uiText = uiAndTtsText[0];
            String ttsText = uiAndTtsText[1];
            TtsText tText = new TtsText(SceneUtil.ICON_MSG, uiText);
            Bundle args = new Bundle();
            args.putParcelable(SceneUtil.EXTRA_TTS_TEXT, tText);
            speak(ttsText, args);
        }
    }
}