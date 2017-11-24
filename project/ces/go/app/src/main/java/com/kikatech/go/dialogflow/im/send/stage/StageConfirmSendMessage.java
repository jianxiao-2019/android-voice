package com.kikatech.go.dialogflow.im.send.stage;

import android.os.Bundle;
import android.support.annotation.NonNull;

import com.kikatech.go.dialogflow.im.IMContent;
import com.kikatech.go.dialogflow.im.send.SceneActions;
import com.kikatech.go.util.LogUtil;
import com.kikatech.voice.core.dialogflow.scene.ISceneFeedback;
import com.kikatech.voice.core.dialogflow.scene.SceneBase;
import com.kikatech.voice.core.dialogflow.scene.SceneStage;

/**
 * Created by brad_chang on 2017/11/23.
 */

public class StageConfirmSendMessage extends BaseSendIMStage {
    StageConfirmSendMessage(@NonNull SceneBase scene, ISceneFeedback feedback) {
        super(scene, feedback);
    }

    @Override
    protected SceneStage getNextStage(String action, Bundle extra) {
        switch (action) {
            case SceneActions.ACTION_SEND_IM_YES:
                return new StageSendIMConfirm(mSceneBase, mFeedback);
            case SceneActions.ACTION_SEND_IM_NO:
            case SceneActions.ACTION_SEND_IM_CHANGE_IM_BODY:
                return new StageConfirmSendMessage(mSceneBase, mFeedback);
            case SceneActions.ACTION_SEND_IM_MSGBODY:
                return new StageConfirmSendMessage(mSceneBase, mFeedback);
            default:
                if (LogUtil.DEBUG) LogUtil.log(TAG, "Unsupported action:" + action);
                break;
        }
        return new StageConfirmSendMessage(mSceneBase, mFeedback);
    }

    @Override
    public void action() {
        IMContent imc = getIMContent();
        speak("Send text \"" + imc.getMessageBody() + "\" to " + imc.getSendTarget() + " ? Send it or Change it ?");
    }
}
