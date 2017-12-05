package com.kikatech.go.dialogflow.sms.send.stage;

import android.os.Bundle;
import android.support.annotation.NonNull;

import com.kikatech.go.dialogflow.sms.send.SceneActions;
import com.kikatech.go.util.LogUtil;
import com.kikatech.voice.core.dialogflow.scene.ISceneFeedback;
import com.kikatech.voice.core.dialogflow.scene.SceneBase;
import com.kikatech.voice.core.dialogflow.scene.SceneStage;

/**
 * Created by brad_chang on 2017/12/5.
 */

public class StageAskAddEmoji extends BaseSendSmsStage {
    StageAskAddEmoji(@NonNull SceneBase scene, ISceneFeedback feedback) {
        super(scene, feedback);
    }

    @Override
    protected SceneStage getNextStage(String action, Bundle extra) {
        switch (action) {
            case SceneActions.ACTION_SEND_SMS_YES:
                getSmsContent().setSendWithEmoji(true);
                return new StageSendSmsConfirm(mSceneBase, mFeedback);
            case SceneActions.ACTION_SEND_SMS_NO:
                //case SceneActions.ACTION_SEND_SMS_CHANGE_SMS_BODY:
                getSmsContent().setSendWithEmoji(false);
                return new StageSendSmsConfirm(mSceneBase, mFeedback);
//            case SceneActions.ACTION_SEND_SMS_MSGBODY:
//                return new StageAskForSendSmsToContact(mSceneBase, mFeedback);
            default:
                if (LogUtil.DEBUG) LogUtil.log(TAG, "Unsupported action:" + action);
                break;
        }
        return this;
    }

    @Override
    public void action() {
        String msg = "Find emoji " + getSmsContent().getEmojiDesc() + ", would you like to add it ?";
        if (LogUtil.DEBUG) {
            LogUtil.log(TAG, msg);
        }
        speak(msg);
    }
}