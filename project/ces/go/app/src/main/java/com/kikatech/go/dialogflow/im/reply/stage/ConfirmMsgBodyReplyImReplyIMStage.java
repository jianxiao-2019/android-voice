package com.kikatech.go.dialogflow.im.reply.stage;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;

import com.kikatech.go.dialogflow.AsrConfigUtil;
import com.kikatech.go.dialogflow.SceneUtil;
import com.kikatech.go.dialogflow.im.reply.SceneActions;
import com.kikatech.go.dialogflow.model.Option;
import com.kikatech.go.dialogflow.model.OptionList;
import com.kikatech.go.util.LogUtil;
import com.kikatech.voice.core.dialogflow.intent.Intent;
import com.kikatech.voice.core.dialogflow.scene.ISceneFeedback;
import com.kikatech.voice.core.dialogflow.scene.SceneBase;
import com.kikatech.voice.core.dialogflow.scene.SceneStage;

/**
 * Created by brad_chang on 2017/11/28.
 */

public class ConfirmMsgBodyReplyImReplyIMStage extends BaseReplyIMStage {

    ConfirmMsgBodyReplyImReplyIMStage(@NonNull SceneBase scene, ISceneFeedback feedback) {
        super(scene, feedback);
        overrideUncaughtAction = true;
    }

    @Override
    @AsrConfigUtil.ASRMode
    protected int getAsrMode() {
        return AsrConfigUtil.ASR_MODE_CONVERSATION_ALTER;
    }

    @Override
    public SceneStage getNextStage(String action, Bundle extra) {
        switch (action) {
            case Intent.ACTION_UNCAUGHT:
            case SceneActions.ACTION_REPLY_IM_YES:
                if (getReplyMessage().hasEmoji()) {
                    return new ReplyIMStageAskAddEmoji(mSceneBase, mFeedback);
                } else {
                    return new SendMessageReplyImReplyIMStage(mSceneBase, mFeedback);
                }
            case SceneActions.ACTION_REPLY_IM_CHANGE:
            case SceneActions.ACTION_REPLY_IM_NO:
                return new AskMsgBodyReplyImReplyIMStage(mSceneBase, mFeedback);
            case Intent.ACTION_RCMD_EMOJI:
                return null;
            default:
                if (LogUtil.DEBUG)
                    LogUtil.logw(TAG, "Unsupported command : " + action + ", ask again");
                return this;
        }
    }

    @Override
    protected boolean supportEmoji() {
        return true;
    }

    @Override
    public void doAction() {
        onStageActionStart();
        action();
    }

    @Override
    public void action() {
//        supportAsrInterrupted = true;
        Context context = mSceneBase.getContext();
        String[] uiAndTtsText = SceneUtil.getConfirmMsg(context, getReplyMessage().getMessageBody());
        if (uiAndTtsText.length > 0) {
            Bundle args = new Bundle();
            String[] options = SceneUtil.getConfirmMsgOptions(context);
            String uiText = uiAndTtsText[0];
            String ttsText = uiAndTtsText[1];
            OptionList optionList = new OptionList(OptionList.REQUEST_TYPE_TEXT);
            optionList.setTitle(uiText);
            optionList.setIconRes(SceneUtil.ICON_MSG);
            for (String option : options) {
                optionList.add(new Option(option, null));
            }
            args.putParcelable(SceneUtil.EXTRA_OPTIONS_LIST, optionList);
            speak(ttsText, args);
        }
    }

    @Override
    public Integer overrideAsrBos() {
        return SceneUtil.CONFIRM_BOS_DURATION;
    }
}
