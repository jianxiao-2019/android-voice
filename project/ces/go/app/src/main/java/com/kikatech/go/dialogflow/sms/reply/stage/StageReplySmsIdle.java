package com.kikatech.go.dialogflow.sms.reply.stage;

import android.os.Bundle;
import android.support.annotation.NonNull;

import com.kikatech.go.dialogflow.AsrConfigUtil;
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

public class StageReplySmsIdle extends BaseReplySmsStage {

    public StageReplySmsIdle(@NonNull SceneBase scene, ISceneFeedback feedback) {
        super(scene, feedback);
    }

    @Override
    protected @AsrConfigUtil.ASRMode int getAsrMode() {
        return AsrConfigUtil.ASR_MODE_CONVERSATION_COMMAND;
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

            updateSmsContent(sms);

            switch (getReplyMsgSetting()) {
                case UserSettings.SETTING_REPLY_MSG_ASK_USER:
                    if (LogUtil.DEBUG) LogUtil.log(TAG, "SETTING_REPLY_MSG_READ");
                    // 3.1
                    return new AskToReadMsgAskStage(mSceneBase, mFeedback);
                case UserSettings.SETTING_REPLY_MSG_READ:
                    if (LogUtil.DEBUG) LogUtil.log(TAG, "SETTING_REPLY_MSG_ASK_USER");
                    // 3.2
                    return new AskToReplySmsReadStage(mSceneBase, mFeedback);
                default:
                case UserSettings.SETTING_REPLY_MSG_IGNORE:
                    if (LogUtil.DEBUG) LogUtil.log(TAG, "Err, Unsupported setting");
                    break;
            }
        }
        return null;
    }
}