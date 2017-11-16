package com.kikatech.go.dialogflow.sms.stage;

import android.os.Bundle;
import android.support.annotation.NonNull;

import com.kikatech.go.dialogflow.sms.SceneSendSms;
import com.kikatech.go.dialogflow.sms.SmsContent;
import com.kikatech.go.dialogflow.sms.SmsSceneActions;
import com.kikatech.go.util.LogUtil;
import com.kikatech.voice.core.dialogflow.scene.ISceneFeedback;
import com.kikatech.voice.core.dialogflow.scene.SceneBase;
import com.kikatech.voice.core.dialogflow.scene.SceneStage;

/**
 * Created by brad_chang on 2017/11/15.
 */

public class StageSendSmsIdle extends BaseSendSmsStage {

    public StageSendSmsIdle(@NonNull SceneBase scene, ISceneFeedback feedback) {
        super(scene, feedback);
    }

    @Override
    public SceneStage next(String action, Bundle extra) {
        super.next(action, extra);

        if (!action.equals(SmsSceneActions.ACTION_SEND_SMS)) {
            if (LogUtil.DEBUG) LogUtil.log(TAG, "Unsupported action:" + action);
            return null;
        }

        SmsContent sc = ((SceneSendSms) mSceneBase).getSmsContent();

        if (!sc.isContactAvailable()) {
            // SendSMS 2.1
            return new StageAskForSendTarget(mSceneBase, mFeedback);
        } else {
            if (sc.isContactMatched()) {
                if (sc.isSimilarContact()) {
                    // SendSMS 2.4
                    return new StageAskSendTargetCorrect(mSceneBase, mFeedback);
                } else {
                    if (sc.hasOnlyOneNumber()) {
                        if (sc.isSmsBodyAvailable()) {
                            // SendSMS 2.9
                            return new StageAskForSendSmsToContact(mSceneBase, mFeedback);
                        } else {
                            // SendSMS 2.8
                            return new StageAskForSmsBody(mSceneBase, mFeedback);
                        }
                    } else {
                        // SendSMS 2.6
                        return new StageAskForChooseNumbers(mSceneBase, mFeedback);
                    }
                }
            } else {
                // SendSMS 2.3
                return new StageAskForSendTarget(mSceneBase, mFeedback);
            }
        }
    }
}