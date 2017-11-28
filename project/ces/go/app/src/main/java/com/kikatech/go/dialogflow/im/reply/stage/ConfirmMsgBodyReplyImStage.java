package com.kikatech.go.dialogflow.im.reply.stage;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;

import com.kikatech.go.dialogflow.SceneUtil;
import com.kikatech.go.dialogflow.im.reply.SceneActions;
import com.kikatech.go.dialogflow.model.Option;
import com.kikatech.go.dialogflow.model.OptionList;
import com.kikatech.go.message.im.BaseIMObject;
import com.kikatech.go.util.LogUtil;
import com.kikatech.voice.core.dialogflow.scene.ISceneFeedback;
import com.kikatech.voice.core.dialogflow.scene.SceneBase;
import com.kikatech.voice.core.dialogflow.scene.SceneStage;

/**
 * Created by brad_chang on 2017/11/28.
 */

public class ConfirmMsgBodyReplyImStage extends BaseStage {
    
    private final String mMsgBody;
    private final BaseIMObject mIMObject;
    
    ConfirmMsgBodyReplyImStage(@NonNull SceneBase scene, ISceneFeedback feedback, BaseIMObject imo, String messageBody) {
        super(scene, feedback);
        mIMObject = imo;
        mMsgBody = messageBody;
    }

    @Override
    public SceneStage getNextStage(String action, Bundle extra) {
        switch (action) {
            case SceneActions.ACTION_REPLY_IM_YES:
                return new SendMessageReplyImStage(mSceneBase, mFeedback, mIMObject, mMsgBody);
            case SceneActions.ACTION_REPLY_IM_CHANGE:
            case SceneActions.ACTION_REPLY_IM_NO:
                return new AskMsgBodyReplyImStage(mSceneBase, mFeedback, mIMObject);
            default:
                if (LogUtil.DEBUG) LogUtil.log(TAG, "Unsupported command : " + action + ", ask again");
                return this;
        }
    }

    @Override
    public void doAction() {
        action();
    }

    @Override
    public void action() {
        Context context = mSceneBase.getContext();
        String[] uiAndTtsText = SceneUtil.getConfirmMsg(context, mMsgBody);
        if (uiAndTtsText.length > 0) {
            Bundle args = new Bundle();
            String[] options = SceneUtil.getConfirmMsgOptions(context);
            String uiText = uiAndTtsText[0];
            String ttsText = uiAndTtsText[1];
            OptionList optionList = new OptionList(OptionList.REQUEST_TYPE_TEXT);
            optionList.setTitle(uiText);
            for (String option : options) {
                optionList.add(new Option(option, null));
            }
            args.putParcelable(SceneUtil.EXTRA_OPTIONS_LIST, optionList);
            speak(ttsText, args);
        }
    }
}
