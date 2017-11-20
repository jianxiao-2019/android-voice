package com.kikatech.go.dialogflow.sms.stage;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.kikatech.go.dialogflow.sms.SmsContent;
import com.kikatech.go.dialogflow.sms.SmsSceneActions;
import com.kikatech.go.util.LogUtil;
import com.kikatech.voice.core.dialogflow.scene.ISceneFeedback;
import com.kikatech.voice.core.dialogflow.scene.SceneBase;
import com.kikatech.voice.core.dialogflow.scene.SceneStage;
import com.kikatech.voice.util.contact.ContactManager;

/**
 * Created by brad_chang on 2017/11/15.
 */

public class StageAskForSendTarget extends BaseSendSmsStage {
    /**
     * SendSMS 2.1 詢問傳送對象 / 2.3 再次詢問對象
     */
    StageAskForSendTarget(@NonNull SceneBase scene, ISceneFeedback feedback) {
        super(scene, feedback);
    }

    @Override
    protected SceneStage getNextStage(String action, Bundle extra) {
        SmsContent sc = getSmsContent();
        boolean supportedCommand = false;
        if (!SmsSceneActions.ACTION_SEND_SMS_NAME.equals(action)) {
            if (SmsSceneActions.ACTION_SEND_SMS_MSGBODY.equals(action) && !TextUtils.isEmpty(sc.getSmsBody())) {
                if (LogUtil.DEBUG)
                    LogUtil.log(TAG, "Try to parse sms content, it might be the contact");
                supportedCommand = sc.tryParseContact(mSceneBase.getContext(), sc.getSmsBody());
                if (LogUtil.DEBUG && !supportedCommand)
                    LogUtil.log(TAG, "" + sc.getSmsBody() + " is not the contact ...");
            }
        } else {
            supportedCommand = true;
        }

        if (supportedCommand) {
            return getStageCheckContactMatched(TAG, sc, mSceneBase, mFeedback);
        } else {
            if (LogUtil.DEBUG) LogUtil.log(TAG, "Unsupported action:" + action);
            return null;
        }
    }

    @Override
    public void action() {
        SmsContent sc = getSmsContent();
        if (sc.isContactAvailable()) {
            // SendSMS 2.3 再次詢問對象
            speak("2.3 Couldn't find " + sc.getContact() + " in contacts. Please say it again.");
        } else {
            // SendSMS 2.1 詢問傳送對象
            speak("2.1 Who to you want to text ?");
        }
    }
}