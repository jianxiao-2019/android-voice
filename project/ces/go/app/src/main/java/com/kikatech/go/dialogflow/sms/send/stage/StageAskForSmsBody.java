package com.kikatech.go.dialogflow.sms.send.stage;

import android.os.Bundle;
import android.support.annotation.NonNull;

import com.kikatech.go.dialogflow.sms.send.SceneActions;
import com.kikatech.go.util.LogUtil;
import com.kikatech.voice.core.dialogflow.scene.ISceneFeedback;
import com.kikatech.voice.core.dialogflow.scene.SceneBase;
import com.kikatech.voice.core.dialogflow.scene.SceneStage;

/**
 * Created by brad_chang on 2017/11/15.
 */

public class StageAskForSmsBody extends BaseSendSmsStage {

    /**
     * SendSMS 2.8 詢問 SMS 內容
     */
    StageAskForSmsBody(@NonNull SceneBase scene, ISceneFeedback feedback) {
        super(scene, feedback);
    }

    @Override
    protected SceneStage getNextStage(String action, Bundle extra) {
        setQueryAnyWords(false);
        if(!action.equals(SceneActions.ACTION_SEND_SMS_MSGBODY)) {
            if (LogUtil.DEBUG) LogUtil.log(TAG, "Unsupported action:" + action);
            return null;
        }

        return getStageCheckSmsBody(TAG, getSmsContent(), mSceneBase, mFeedback);
    }

    @Override
    public void action() {
        setQueryAnyWords(true);
        String speech = "Please say your message."; // doc 21
        speak(speech);
    }
}
