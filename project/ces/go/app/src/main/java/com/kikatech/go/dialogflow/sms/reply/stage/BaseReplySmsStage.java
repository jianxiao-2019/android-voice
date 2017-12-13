package com.kikatech.go.dialogflow.sms.reply.stage;

import android.os.Bundle;
import android.support.annotation.NonNull;

import com.kikatech.go.dialogflow.BaseSceneStage;
import com.kikatech.go.dialogflow.UserSettings;
import com.kikatech.go.dialogflow.im.reply.SceneReplyIM;
import com.kikatech.go.dialogflow.sms.ReplySmsMessage;
import com.kikatech.go.dialogflow.sms.reply.SceneReplySms;
import com.kikatech.go.message.sms.SmsObject;
import com.kikatech.go.util.LogUtil;
import com.kikatech.voice.core.dialogflow.intent.Intent;
import com.kikatech.voice.core.dialogflow.scene.ISceneFeedback;
import com.kikatech.voice.core.dialogflow.scene.SceneBase;
import com.kikatech.voice.core.dialogflow.scene.SceneStage;

/**
 * Created by brad_chang on 2017/11/20.
 */

public class BaseReplySmsStage extends BaseSceneStage {

    BaseReplySmsStage(@NonNull SceneBase scene, ISceneFeedback feedback) {
        super(scene, feedback);
        if (LogUtil.DEBUG) LogUtil.log(TAG, "init");
    }

    SmsObject getReceivedSms(long timestamp) {
        return ((SceneReplySms) mSceneBase).getReceivedSms(timestamp);
    }

    public ReplySmsMessage getReplyMessage() {
        return ((SceneReplySms) mSceneBase).getReplyMessage();
    }

    void updateSmsContent(SmsObject sms) {
        ((SceneReplySms) mSceneBase).updateSmsContent(sms);
    }

    @Override
    public SceneStage next(String action, Bundle extra) {
        if (action.equals(Intent.ACTION_RCMD_EMOJI)) {
            String emojiJson = Intent.parseEmojiJsonString(extra);
            ((SceneReplySms) mSceneBase).updateEmoji(emojiJson);
        } else if (UserSettings.getReplyMessageSetting() == UserSettings.SETTING_REPLY_SMS_IGNORE) {
            if (LogUtil.DEBUG) LogUtil.log(TAG, "SETTING_REPLY_SMS_IGNORE");
            exitScene();
            return null;
        }
        return getNextStage(action, extra);
    }

    protected SceneStage getNextStage(String action, Bundle extra) {
        return null;
    }

    @Override
    public void prepare() {
        if (LogUtil.DEBUG) LogUtil.log(TAG, "prepare");
    }

    @Override
    public void action() {
        if (LogUtil.DEBUG) LogUtil.log(TAG, "action");
    }
}