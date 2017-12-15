package com.kikatech.go.dialogflow.telephony.incoming.stage;

import android.os.Bundle;

import com.kikatech.go.dialogflow.AsrConfigUtil;
import com.kikatech.go.dialogflow.BaseSceneStage;
import com.kikatech.go.dialogflow.SceneUtil;
import com.kikatech.go.dialogflow.model.TtsText;
import com.kikatech.go.dialogflow.telephony.incoming.SceneActions;
import com.kikatech.go.util.AudioManagerUtil;
import com.kikatech.voice.core.dialogflow.scene.ISceneFeedback;
import com.kikatech.voice.core.dialogflow.scene.SceneBase;
import com.kikatech.voice.core.dialogflow.scene.SceneStage;

/**
 * Created by tianli on 17-11-11.
 */

public class StageIncoming extends BaseSceneStage {

    private String mCaller;

    public StageIncoming(SceneBase scene, ISceneFeedback feedback, String caller) {
        super(scene, feedback);
        mCaller = caller;
    }

    @Override
    protected @AsrConfigUtil.ASRMode int getAsrMode() {
        return AsrConfigUtil.ASR_MODE_SHORT_COMMAND;
    }

    @Override
    public SceneStage next(String action, Bundle extra) {
        if (SceneActions.ACTION_INCOMING_ANSWER.equals(action)) {
            return new StageAnswer(mSceneBase, mFeedback);
        } else if (SceneActions.ACTION_INCOMING_REJECT.equals(action)) {
            return new StageReject(mSceneBase, mFeedback);
        } else if (SceneActions.ACTION_INCOMING_IGNORE.equals(action)) {
            return new StageIgnore(mSceneBase, mFeedback);
        }
        return null;
    }

    @Override
    public void prepare() {
    }

    @Override
    public void doAction() {
        onStageActionStart();
        action();
    }

    @Override
    public void action() {
        AudioManagerUtil.getIns().muteRing();
        String[] uiAndTtsText = SceneUtil.getAskActionForIncoming(mSceneBase.getContext(), mCaller);
        if (uiAndTtsText.length > 0) {
            String uiText = uiAndTtsText[0];
            String ttsText = uiAndTtsText[1];
            TtsText tText = new TtsText(SceneUtil.ICON_TELEPHONY, uiText);
            Bundle args = new Bundle();
            args.putParcelable(SceneUtil.EXTRA_TTS_TEXT, tText);
            speak(ttsText, args);
        }
    }

    @Override
    public void onStageActionDone(boolean isInterrupted) {
        super.onStageActionDone(isInterrupted);
    }
}
