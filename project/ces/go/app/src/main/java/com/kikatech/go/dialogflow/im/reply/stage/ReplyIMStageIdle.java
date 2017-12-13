package com.kikatech.go.dialogflow.im.reply.stage;

import android.os.Bundle;
import android.support.annotation.NonNull;

import com.kikatech.go.dialogflow.AsrConfigUtil;
import com.kikatech.go.dialogflow.UserSettings;
import com.kikatech.go.dialogflow.im.reply.SceneActions;
import com.kikatech.go.dialogflow.sms.SmsUtil;
import com.kikatech.go.message.im.BaseIMObject;
import com.kikatech.go.util.LogUtil;
import com.kikatech.voice.core.dialogflow.scene.ISceneFeedback;
import com.kikatech.voice.core.dialogflow.scene.SceneBase;
import com.kikatech.voice.core.dialogflow.scene.SceneStage;

/**
 * Created by brad_chang on 2017/11/23.
 */

public class ReplyIMStageIdle extends BaseReplyIMStage {
    public ReplyIMStageIdle(@NonNull SceneBase scene, ISceneFeedback feedback) {
        super(scene, feedback);
    }

    @Override
    protected @AsrConfigUtil.ASRMode int getAsrMode() {
        return AsrConfigUtil.ASR_MODE_CONVERSATION_COMMAND;
    }

    @Override
    public SceneStage getNextStage(String action, Bundle extra) {
        if (action.equals(SceneActions.ACTION_REPLY_IM)) {

            long timestamp = -1;
            String s = SmsUtil.parseTagAny(extra);
            try {
                timestamp = Long.parseLong(s);
            } catch (Exception e) {
                if (LogUtil.DEBUG)
                    LogUtil.log(TAG, "Parse timestamp error, timestamp:" + s);
            }
            BaseIMObject imo = getReceivedIM(timestamp);
            if (imo == null) {
                if (LogUtil.DEBUG) LogUtil.log(TAG, "Err, no message @ idx " + timestamp);
                return null;
            }

            updateIMContent(imo);

            // TODO Check setting
            byte rms = UserSettings.getReplyMessageSetting();
            if (rms == UserSettings.SETTING_REPLY_SMS_ASK_USER) {
                if (LogUtil.DEBUG) LogUtil.log(TAG, "SETTING_REPLY_SMS_READ");
                // 7.1
                return new AskToReadContentReplyIMStage(mSceneBase, mFeedback);
            } else if (rms == UserSettings.SETTING_REPLY_SMS_READ) {
                if (LogUtil.DEBUG) LogUtil.log(TAG, "SETTING_REPLY_SMS_ASK_USER");
                // 7.2
                return new ReadContentAndAskToReplyImReplyIMStage(mSceneBase, mFeedback);
            } else {
                if (LogUtil.DEBUG) LogUtil.logw(TAG, "Err, Unsupported setting:" + rms);
            }
        }
        return null;
    }
}