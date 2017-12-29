package com.kikatech.go.dialogflow.im.send.stage;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;

import com.kikatech.go.dialogflow.SceneUtil;
import com.kikatech.go.dialogflow.UserSettings;
import com.kikatech.go.dialogflow.im.send.IMContent;
import com.kikatech.go.dialogflow.im.send.SceneActions;
import com.kikatech.go.dialogflow.model.Option;
import com.kikatech.go.dialogflow.model.OptionList;
import com.kikatech.go.util.LogUtil;
import com.kikatech.voice.core.dialogflow.intent.Intent;
import com.kikatech.voice.core.dialogflow.scene.ISceneFeedback;
import com.kikatech.voice.core.dialogflow.scene.SceneBase;
import com.kikatech.voice.core.dialogflow.scene.SceneStage;

/**
 * Created by brad_chang on 2017/11/23.
 */

public class StageConfirmSendMessage extends BaseSendIMStage {
    StageConfirmSendMessage(@NonNull SceneBase scene, ISceneFeedback feedback) {
        super(scene, feedback);
        overrideUncaughtAction = UserSettings.getSettingConfirmCounter();
    }

    @Override
    protected SceneStage getNextStage(String action, Bundle extra) {
        switch (action) {
            case Intent.ACTION_UNCAUGHT:
            case SceneActions.ACTION_SEND_IM_YES:
                if (getIMContent().hasEmoji()) {
                    return new StageAskAddEmoji(mSceneBase, mFeedback);
                } else {
                    return new StageSendIMConfirm(mSceneBase, mFeedback);
                }
            case SceneActions.ACTION_SEND_IM_NO:
            case SceneActions.ACTION_SEND_IM_CHANGE_IM_BODY:
                return new StageAskMsgBody(mSceneBase, mFeedback);
            case SceneActions.ACTION_SEND_IM_MSGBODY:
                return new StageConfirmSendMessage(mSceneBase, mFeedback);
            case Intent.ACTION_RCMD_EMOJI:
                return null;
            default:
                if (LogUtil.DEBUG) LogUtil.log(TAG, "Unsupported action:" + action);
                break;
        }
        return this;
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
        IMContent imc = getIMContent();
        Context context = mSceneBase.getContext();
        String[] uiAndTtsText = SceneUtil.getConfirmMsg(context, imc.getMessageBody());
        if (uiAndTtsText.length > 0) {
            Bundle args = new Bundle();
            String[] options = SceneUtil.getOptionsCommon(context);
            requestAsrAlignment(options);
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
        return overrideUncaughtAction ? SceneUtil.CONFIRM_BOS_DURATION : null;
    }
}
