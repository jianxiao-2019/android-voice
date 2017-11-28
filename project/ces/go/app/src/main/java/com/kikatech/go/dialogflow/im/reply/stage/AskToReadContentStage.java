package com.kikatech.go.dialogflow.im.reply.stage;

import android.os.Bundle;
import android.support.annotation.NonNull;

import com.kikatech.go.dialogflow.SceneUtil;
import com.kikatech.go.dialogflow.im.reply.SceneActions;
import com.kikatech.go.dialogflow.model.UserInfo;
import com.kikatech.go.message.im.BaseIMObject;
import com.kikatech.go.util.LogUtil;
import com.kikatech.voice.core.dialogflow.scene.ISceneFeedback;
import com.kikatech.voice.core.dialogflow.scene.SceneBase;
import com.kikatech.voice.core.dialogflow.scene.SceneStage;

/**
 * Created by brad_chang on 2017/11/28.
 */

public class AskToReadContentStage extends BaseStage {

    private final BaseIMObject mIMObject;

    AskToReadContentStage(@NonNull SceneBase scene, ISceneFeedback feedback, BaseIMObject imo) {
        super(scene, feedback);
        mIMObject = imo;
    }

    @Override
    public SceneStage getNextStage(String action, Bundle extra) {
        switch (action) {
            case SceneActions.ACTION_REPLY_IM_YES:
                return new ReadContentAndAskToReplyImStage(mSceneBase, mFeedback, mIMObject);
            case SceneActions.ACTION_REPLY_IM_NO:
            case SceneActions.ACTION_REPLY_IM_CANCEL:
                if (LogUtil.DEBUG) LogUtil.log(TAG, "Stop !!");
                exitScene();
                return null;
        }
        return this;
    }

    @Override
    public void doAction() {
        action();
    }

    @Override
    protected void action() {
        if (LogUtil.DEBUG) {
            LogUtil.log(TAG, "Receive message form " + mIMObject.getUserName() + ", do you want to play the message ?");
        }
        String name = mIMObject.isGroup() ? mIMObject.getGroupName() : mIMObject.getUserName();
        String ttsText = SceneUtil.getNewMsgUsrInfo(mSceneBase.getContext(), mIMObject.getUserName());
        UserInfo userInfo = new UserInfo(mIMObject.getAvatarFilePath(), name, mIMObject.getAppInfo());
        Bundle args = new Bundle();
        args.putParcelable(SceneUtil.EXTRA_USR_INFO, userInfo);
        speak(ttsText, args);
    }

    @Override
    public void onStageActionDone(boolean isInterrupted) {
        if (!isInterrupted) {
            mSceneBase.nextStage(new AskToReadContentOptionStage(mSceneBase, mFeedback, mIMObject));
        }
        super.onStageActionDone(true);
    }
}
