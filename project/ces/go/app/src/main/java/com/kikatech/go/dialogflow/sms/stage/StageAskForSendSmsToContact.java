package com.kikatech.go.dialogflow.sms.stage;

import android.os.Bundle;
import android.support.annotation.NonNull;

import com.kikatech.go.dialogflow.sms.SmsContent;
import com.kikatech.go.dialogflow.sms.SmsSceneActions;
import com.kikatech.go.util.LogUtil;
import com.kikatech.voice.core.dialogflow.scene.ISceneFeedback;
import com.kikatech.voice.core.dialogflow.scene.SceneBase;
import com.kikatech.voice.core.dialogflow.scene.SceneStage;

/**
 * Created by brad_chang on 2017/11/15.
 */

public class StageAskForSendSmsToContact extends BaseSendSmsStage {

    /**
     * SendSMS 2.9 確認訊息內容
     */
    StageAskForSendSmsToContact(@NonNull SceneBase scene, ISceneFeedback feedback) {
        super(scene, feedback);
    }

    @Override
    protected SceneStage getNextStage(String action, Bundle extra) {
        switch (action) {
            case SmsSceneActions.ACTION_SEND_SMS_YES:
                return new StageSendSmsConfirm(mSceneBase, mFeedback);
            case SmsSceneActions.ACTION_SEND_SMS_NO:
                return new StageAskForSmsBody(mSceneBase, mFeedback);
            case SmsSceneActions.ACTION_SEND_SMS_MSGBODY:
                return new StageAskForSendSmsToContact(mSceneBase, mFeedback);
            default:
                if (LogUtil.DEBUG) LogUtil.log(TAG, "Unsupported action:" + action);
                break;
        }
        return null;
    }

    @Override
    public void action() {
        SmsContent sc = getSmsContent();
        speak("2.9 Are you sure to send text " + sc.getSmsBody() + " to " + sc.getMatchedName() + " ?");
    }
}
