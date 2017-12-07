package com.kikatech.go.dialogflow.im.reply.stage;

import android.os.Bundle;
import android.support.annotation.NonNull;

import com.kikatech.go.dialogflow.UserSettings;
import com.kikatech.go.dialogflow.im.reply.ReplyIMMessage;
import com.kikatech.go.dialogflow.im.reply.SceneReplyIM;
import com.kikatech.go.message.im.BaseIMObject;
import com.kikatech.go.util.LogUtil;
import com.kikatech.voice.core.dialogflow.intent.Intent;
import com.kikatech.voice.core.dialogflow.scene.ISceneFeedback;
import com.kikatech.voice.core.dialogflow.scene.SceneBase;
import com.kikatech.voice.core.dialogflow.scene.SceneStage;

/**
 * Created by brad_chang on 2017/11/28.
 */

public class BaseStage extends SceneStage {

    BaseStage(@NonNull SceneBase scene, ISceneFeedback feedback) {
        super(scene, feedback);
        if (LogUtil.DEBUG) LogUtil.log(TAG, "init");
    }

    ReplyIMMessage getReplyMessage() {
        if (LogUtil.DEBUG) {
            LogUtil.log(TAG, ((SceneReplyIM) mSceneBase).getReplyMessage().toString());
        }
        return ((SceneReplyIM) mSceneBase).getReplyMessage();
    }

    public void updateIMContent(BaseIMObject imo) {
        ((SceneReplyIM) mSceneBase).updateIMContent(imo);
    }

    @Override
    public SceneStage next(String action, Bundle extra) {
        if (action.equals(Intent.ACTION_RCMD_EMOJI)) {
            String emojiJson = Intent.parseEmojiJsonString(extra);
            ((SceneReplyIM) mSceneBase).updateEmoji(emojiJson);
        } else if (UserSettings.getReplyMessageSetting() == UserSettings.SETTING_REPLY_SMS_IGNORE) {
            if (LogUtil.DEBUG) LogUtil.log(TAG, "SETTING_REPLY_SMS_IGNORE");
            exitScene();
            return null;
        }
        if (LogUtil.DEBUG) LogUtil.log(TAG, "action:" + action);
        return getNextStage(action, extra);
    }

    BaseIMObject getReceivedIM(long timestamp) {
        return ((SceneReplyIM) mSceneBase).getReceivedIM(timestamp);
    }

    protected SceneStage getNextStage(String action, Bundle extra) {
        return null;
    }

    @Override
    protected void prepare() {

    }

    @Override
    protected void action() {

    }
}
