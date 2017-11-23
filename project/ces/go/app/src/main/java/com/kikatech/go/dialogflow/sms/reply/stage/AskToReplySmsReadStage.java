package com.kikatech.go.dialogflow.sms.reply.stage;

import android.os.Bundle;
import android.support.annotation.NonNull;

import com.kikatech.go.dialogflow.SceneUtil;
import com.kikatech.go.dialogflow.sms.reply.SceneActions;
import com.kikatech.go.message.sms.SmsObject;
import com.kikatech.go.util.LogUtil;
import com.kikatech.voice.core.dialogflow.scene.IDialogFlowFeedback;
import com.kikatech.voice.core.dialogflow.scene.ISceneFeedback;
import com.kikatech.voice.core.dialogflow.scene.SceneBase;
import com.kikatech.voice.core.dialogflow.scene.SceneStage;

/**
 * Created by brad_chang on 2017/11/20.
 */

public class AskToReplySmsReadStage extends BaseReplySmsStage {

    private final SmsObject mSmsObject;

    AskToReplySmsReadStage(@NonNull SceneBase scene, ISceneFeedback feedback, @NonNull SmsObject sms) {
        super(scene, feedback);
        mSmsObject = sms;
    }

    @Override
    public SceneStage getNextStage(String action, Bundle extra) {
        switch (action) {
            case SceneActions.ACTION_REPLY_SMS_YES:
                return new AskMsgBodyReplySmsStage(mSceneBase, mFeedback, mSmsObject);
            case SceneActions.ACTION_REPLY_SMS_NO:
            case SceneActions.ACTION_REPLY_SMS_CANCEL:
                if (LogUtil.DEBUG) LogUtil.log(TAG, "Stop !!");
                exitScene();
                break;
        }
        return null;
    }

    @Override
    public void action() {
        String[] uiAndTtsText = SceneUtil.getReadMsg(mSceneBase.getContext(), mSmsObject.getUserName(), mSmsObject.getMsgContent());
        if (uiAndTtsText.length > 0) {
            String uiText = uiAndTtsText[0];
            String ttsText = uiAndTtsText[1];
            Bundle args = new Bundle();
            args.putString(SceneUtil.EXTRA_UI_TEXT, uiText);
            speak(ttsText, args, new IDialogFlowFeedback.IToSceneFeedback() {
                @Override
                public void onTtsStart() {
                }

                @Override
                public void onTtsComplete() {
                    goToAskReplyStage();
                }

                @Override
                public void onTtsError() {
                    goToAskReplyStage();
                }

                @Override
                public void onTtsInterrupted() {
                }

                @Override
                public boolean isEndOfScene() {
                    return false;
                }
            });
        }
//        String msg = mSmsObject.getUserName() + " said : \"" + mSmsObject.getMsgContent() + "\", Do you want to reply ?";
//        if (LogUtil.DEBUG) LogUtil.log(TAG, msg);
//        speak(msg);

//        String speech = String.format("%1$s says \"%2$s\". Do you want to reply?", mSmsObject.getUserName(), mSmsObject.getMsgContent()); // doc 24
//        speak(speech);
    }

    private void goToAskReplyStage() {
        mSceneBase.nextStage(new AskToReplySmsOptionStage(mSceneBase, mFeedback, mSmsObject));
    }
}
