package com.kikatech.go.dialogflow.sms.reply.stage;

import android.os.Bundle;
import android.support.annotation.NonNull;

import com.kikatech.go.dialogflow.sms.reply.SceneActions;
import com.kikatech.go.util.LogUtil;
import com.kikatech.voice.core.dialogflow.scene.ISceneFeedback;
import com.kikatech.voice.core.dialogflow.scene.SceneBase;
import com.kikatech.voice.core.dialogflow.scene.SceneStage;

/**
 * Created by brad_chang on 2017/12/7.
 */

public class StageUpdateEmoji extends BaseReplySmsStage {
    StageUpdateEmoji(@NonNull SceneBase scene, ISceneFeedback feedback) {
        super(scene, feedback);
    }

    @Override
    protected SceneStage getNextStage(String action, Bundle extra) {
        getReplyMessage().setSendWithEmoji(false);
        switch (action) {
            case SceneActions.ACTION_REPLY_SMS_YES:
                if(getReplyMessage().hasEmoji()) {
                    return new StageAskAddEmoji(mSceneBase, mFeedback);
                } else {
                    return new SendMessageReplySmsStage(mSceneBase, mFeedback);
                }
            case SceneActions.ACTION_REPLY_SMS_NO:
                return new SendMessageReplySmsStage(mSceneBase, mFeedback);
            default:
                if (LogUtil.DEBUG)
                    LogUtil.log(TAG, "Unsupported command : " + action + ", ask again");
                return new SendMessageReplySmsStage(mSceneBase, mFeedback);
        }
    }

    @Override
    public void doAction() {
    }

    @Override
    public void action() {
        if (LogUtil.DEBUG) {
            LogUtil.log(TAG, "Emoji, updated : " + getReplyMessage().getEmojiUnicode() + " <" + getReplyMessage().getEmojiDesc() + ">");
        }
    }
}