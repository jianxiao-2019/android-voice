package com.kikatech.go.dialogflow.sms.reply.stage;

import android.os.Bundle;
import android.support.annotation.NonNull;

import com.kikatech.go.dialogflow.UserSettings;
import com.kikatech.go.dialogflow.sms.SmsUtil;
import com.kikatech.go.dialogflow.sms.reply.SceneActions;
import com.kikatech.go.message.sms.SmsObject;
import com.kikatech.go.util.LogUtil;
import com.kikatech.voice.core.dialogflow.scene.ISceneFeedback;
import com.kikatech.voice.core.dialogflow.scene.SceneBase;
import com.kikatech.voice.core.dialogflow.scene.SceneStage;

/**
 * Created by brad_chang on 2017/11/17.
 */

public class StageIdle extends BaseReplySmsStage {

    public StageIdle(@NonNull SceneBase scene, ISceneFeedback feedback) {
        super(scene, feedback);
    }

    @Override
    public SceneStage getNextStage(String action, Bundle extra) {
        if (action.equals(SceneActions.ACTION_REPLY_SMS)) {

            long timestamp = -1;
            String s = SmsUtil.parseTagAny(extra);
            try {
                timestamp = Long.parseLong(s);
            } catch (Exception e) {
                if (LogUtil.DEBUG)
                    LogUtil.log(TAG, "Parse timestamp error, timestamp:" + s);
            }
            SmsObject sms = getReceivedSms(timestamp);
            if (sms == null) {
                if (LogUtil.DEBUG) LogUtil.log(TAG, "Err, no message @ idx " + timestamp);
                return null;
            }

            // TODO Check setting
            byte rms = UserSettings.getReplyMessageSetting();
            if (rms == UserSettings.SETTING_REPLY_SMS_ASK_USER) {
                if (LogUtil.DEBUG) LogUtil.log(TAG, "SETTING_REPLY_SMS_READ");
                // 3.1
                return new AskToReadMsgAskStage(mSceneBase, mFeedback, sms);
            } else if (rms == UserSettings.SETTING_REPLY_SMS_READ) {
                if (LogUtil.DEBUG) LogUtil.log(TAG, "SETTING_REPLY_SMS_ASK_USER");
                // 3.2
                return new AskToReplySmsReadStage(mSceneBase, mFeedback, sms);
            } else {
                if (LogUtil.DEBUG) LogUtil.log(TAG, "Err, Unsupported setting:" + rms);
            }
        }
        return null;
    }
}