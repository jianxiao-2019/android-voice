package com.kikatech.go.dialogflow.im.send.stage;

import android.os.Bundle;
import android.support.annotation.NonNull;

import com.kikatech.go.dialogflow.im.send.SceneActions;
import com.kikatech.go.util.LogUtil;
import com.kikatech.voice.core.dialogflow.scene.ISceneFeedback;
import com.kikatech.voice.core.dialogflow.scene.SceneBase;
import com.kikatech.voice.core.dialogflow.scene.SceneStage;

/**
 * Created by brad_chang on 2017/12/6.
 */

public class StageUpdateEmoji extends BaseSendIMStage {
    StageUpdateEmoji(@NonNull SceneBase scene, ISceneFeedback feedback) {
        super(scene, feedback);
    }

    @Override
    protected SceneStage getNextStage(String action, Bundle extra) {
        switch (action) {
            case SceneActions.ACTION_SEND_IM_YES:
                return new StageAskAddEmoji(mSceneBase, mFeedback);
            case SceneActions.ACTION_SEND_IM_NO:
                return new StageAskMsgBody(mSceneBase, mFeedback);
            case SceneActions.ACTION_SEND_IM_MSGBODY:
                return new StageAskSendTarget(mSceneBase, mFeedback);
            default:
                if (LogUtil.DEBUG) LogUtil.log(TAG, "Unsupported action:" + action);
                break;
        }
        return this;
    }

    @Override
    public void doAction() {
    }

    @Override
    public void action() {
        if (LogUtil.DEBUG) {
            LogUtil.log(TAG, "Emoji, updated : " + getIMContent().getEmojiUnicode() + " <" + getIMContent().getEmojiDesc() + ">");
        }
    }
}
