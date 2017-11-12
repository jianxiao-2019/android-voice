package com.kikatech.voice.dialogflow.telephony.incoming.stage;

import android.os.Bundle;

import com.kikatech.voice.core.dialogflow.scene.ISceneFeedback;
import com.kikatech.voice.core.dialogflow.scene.SceneStage;
import com.kikatech.voice.dialogflow.telephony.incoming.SceneActions;

/**
 * Created by tianli on 17-11-11.
 */

public class StageIncoming extends SceneStage {

    private String mCaller;

    public StageIncoming(ISceneFeedback feedback, String caller) {
        super(feedback);
        mCaller = caller;
    }

    @Override
    public SceneStage next(String action, Bundle extra) {
        if (SceneActions.ACTION_INCOMING_ANSWER.equals(action)) {
            return new StageAnswer(mFeedback);
        } else if (SceneActions.ACTION_INCOMING_REJECT.equals(action)) {
            return new StageReject(mFeedback);
        } else if (SceneActions.ACTION_INCOMING_IGNORE.equals(action)) {
            return new StageIgnore(mFeedback);
        }
        return null;
    }

    @Override
    public void action() {
        String toast = String.format("%s is calling you", mCaller);
        speak(toast);
    }
}
