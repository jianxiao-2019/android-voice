package com.kikatech.go.dialogflow.im.reply.stage;

import android.os.Bundle;
import android.support.annotation.NonNull;

import com.kikatech.go.dialogflow.im.reply.SceneActions;
import com.kikatech.go.util.LogUtil;
import com.kikatech.voice.core.dialogflow.scene.ISceneFeedback;
import com.kikatech.voice.core.dialogflow.scene.SceneBase;
import com.kikatech.voice.core.dialogflow.scene.SceneStage;

/**
 * Created by brad_chang on 2017/12/7.
 */

public class ReplyIMStageUpdateEmoji extends BaseReplyIMStage {
    ReplyIMStageUpdateEmoji(@NonNull SceneBase scene, ISceneFeedback feedback) {
        super(scene, feedback);
    }

    @Override
    protected SceneStage getNextStage(String action, Bundle extra) {
        switch (action) {
            case SceneActions.ACTION_REPLY_IM_YES:
                if(getReplyMessage().hasEmoji()) {
                    return new ReplyIMStageAskAddEmoji(mSceneBase, mFeedback);
                } else {
                    return new SendMessageReplyImReplyIMStage(mSceneBase, mFeedback);
                }
            case SceneActions.ACTION_REPLY_IM_CHANGE:
            case SceneActions.ACTION_REPLY_IM_NO:
                return new AskMsgBodyReplyImReplyIMStage(mSceneBase, mFeedback);
            default:
                if (LogUtil.DEBUG) LogUtil.logw(TAG, "Unsupported command : " + action + ", ask again");
                return new AskMsgBodyReplyImReplyIMStage(mSceneBase, mFeedback);
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
