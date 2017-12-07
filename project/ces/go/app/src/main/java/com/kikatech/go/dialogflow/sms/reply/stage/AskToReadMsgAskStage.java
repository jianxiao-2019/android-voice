package com.kikatech.go.dialogflow.sms.reply.stage;

import android.os.Bundle;
import android.support.annotation.NonNull;

import com.kikatech.go.dialogflow.SceneUtil;
import com.kikatech.go.dialogflow.model.UserInfo;
import com.kikatech.go.dialogflow.sms.reply.SceneActions;
import com.kikatech.go.message.sms.SmsObject;
import com.kikatech.go.util.AppInfo;
import com.kikatech.go.util.LogUtil;
import com.kikatech.voice.core.dialogflow.scene.ISceneFeedback;
import com.kikatech.voice.core.dialogflow.scene.SceneBase;
import com.kikatech.voice.core.dialogflow.scene.SceneStage;

/**
 * Created by brad_chang on 2017/11/20.
 */

public class AskToReadMsgAskStage extends BaseReplySmsStage {

    private final SmsObject mSmsObject;

    AskToReadMsgAskStage(@NonNull SceneBase scene, ISceneFeedback feedback, @NonNull SmsObject sms) {
        super(scene, feedback);
        mSmsObject = sms;
    }

    @Override
    public SceneStage getNextStage(String action, Bundle extra) {
        switch (action) {
            case SceneActions.ACTION_REPLY_SMS_YES:
                return new AskToReplySmsReadStage(mSceneBase, mFeedback, mSmsObject);
            case SceneActions.ACTION_REPLY_SMS_NO:
            case SceneActions.ACTION_REPLY_SMS_CANCEL:
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
    public void action() {
        String ttsText = SceneUtil.getNewMsgUsrInfo(mSceneBase.getContext(), mSmsObject.getUserName());
        UserInfo userInfo = new UserInfo(mSmsObject.getPhotoUri(), mSmsObject.getUserName(), AppInfo.SMS);
        Bundle args = new Bundle();
        args.putParcelable(SceneUtil.EXTRA_USR_INFO, userInfo);
        speak(ttsText, args);
    }

    @Override
    public void onStageActionDone(boolean isInterrupted) {
        mSceneBase.nextStage(new AskToReadMsgOptionStage(mSceneBase, mFeedback, mSmsObject));
    }
}
