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

    AskToReadContentStage(@NonNull SceneBase scene, ISceneFeedback feedback) {
        super(scene, feedback);
    }

    @Override
    public SceneStage getNextStage(String action, Bundle extra) {
        switch (action) {
            case SceneActions.ACTION_REPLY_IM_YES:
                return new ReadContentAndAskToReplyImStage(mSceneBase, mFeedback);
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
        BaseIMObject iMObject = getReplyMessage().getIMObject();
        if (LogUtil.DEBUG) {
            LogUtil.log(TAG, "Receive message form " + iMObject.getUserName() + ", do you want to play the message ?");
        }
        String name = iMObject.isGroup() ? iMObject.getGroupName() : iMObject.getUserName();
        String ttsText = SceneUtil.getNewMsgUsrInfo(mSceneBase.getContext(), iMObject.getUserName());
        UserInfo userInfo = new UserInfo(iMObject.getAvatarFilePath(), name, iMObject.getAppInfo());
        Bundle args = new Bundle();
        args.putParcelable(SceneUtil.EXTRA_USR_INFO, userInfo);
        speak(ttsText, args);
    }

    @Override
    public void onStageActionDone(boolean isInterrupted) {
        mSceneBase.nextStage(new AskToReadContentOptionStage(mSceneBase, mFeedback));
    }
}
