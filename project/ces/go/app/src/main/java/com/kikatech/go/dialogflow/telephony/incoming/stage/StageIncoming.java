package com.kikatech.go.dialogflow.telephony.incoming.stage;

import android.os.Bundle;

import com.kikatech.go.dialogflow.SceneUtil;
import com.kikatech.go.util.AudioManagerUtil;
import com.kikatech.voice.core.dialogflow.scene.IDialogFlowFeedback;
import com.kikatech.voice.core.dialogflow.scene.ISceneFeedback;
import com.kikatech.voice.core.dialogflow.scene.SceneBase;
import com.kikatech.voice.core.dialogflow.scene.SceneStage;
import com.kikatech.go.dialogflow.telephony.incoming.SceneActions;

/**
 * Created by tianli on 17-11-11.
 */

public class StageIncoming extends SceneStage {

    private String mCaller;

    public StageIncoming(SceneBase scene, ISceneFeedback feedback, String caller) {
        super(scene, feedback);
        mCaller = caller;
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
    public void action() {
        String[] uiAndTtsText = SceneUtil.getAskActionForIncoming(mSceneBase.getContext(), mCaller);
        if (uiAndTtsText.length > 0) {
            String uiText = uiAndTtsText[0];
            String ttsText = uiAndTtsText[1];
            Bundle args = new Bundle();
            args.putString(SceneUtil.EXTRA_UI_TEXT, uiText);

            AudioManagerUtil.getIns().muteRing();
            speak(ttsText, args, new IDialogFlowFeedback.IToSceneFeedback() {
                @Override
                public void onTtsStart() {
                }

                @Override
                public void onTtsComplete() {
                    AudioManagerUtil.getIns().unmuteRing();
                }

                @Override
                public void onTtsError() {
                    AudioManagerUtil.getIns().unmuteRing();
                }

                @Override
                public void onTtsInterrupted() {
                    AudioManagerUtil.getIns().unmuteRing();
                }

                @Override
                public boolean isEndOfScene() {
                    return false;
                }
            });
        }
    }
}
