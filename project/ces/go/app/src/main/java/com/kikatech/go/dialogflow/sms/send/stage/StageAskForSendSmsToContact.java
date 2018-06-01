package com.kikatech.go.dialogflow.sms.send.stage;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;

import com.kikatech.go.dialogflow.AsrConfigUtil;
import com.kikatech.go.dialogflow.SceneUtil;
import com.kikatech.go.dialogflow.UserSettings;
import com.kikatech.go.dialogflow.model.Option;
import com.kikatech.go.dialogflow.model.OptionList;
import com.kikatech.go.dialogflow.sms.SmsContent;
import com.kikatech.go.dialogflow.sms.send.SceneActions;
import com.kikatech.go.util.LogUtil;
import com.kikatech.voice.core.dialogflow.intent.Intent;
import com.kikatech.voice.core.dialogflow.scene.ISceneFeedback;
import com.kikatech.voice.core.dialogflow.scene.SceneBase;
import com.kikatech.voice.core.dialogflow.scene.SceneStage;

/**
 * Created by brad_chang on 2017/11/15.
 */

public class StageAskForSendSmsToContact extends BaseSendSmsStage {

    /**
     * SendSMS 2.9 確認訊息內容
     */
    StageAskForSendSmsToContact(@NonNull SceneBase scene, ISceneFeedback feedback) {
        super(scene, feedback);
        overrideUncaughtAction = UserSettings.getSettingConfirmCounter();
    }

    @Override
    @AsrConfigUtil.ASRMode
    protected int getAsrMode() {
        return AsrConfigUtil.ASR_MODE_CONVERSATION_ALTER;
    }

    @Override
    protected SceneStage getNextStage(String action, Bundle extra) {
        cancelAsrAlignment();
        SmsContent sc = getSmsContent();
        switch (action) {
            case Intent.ACTION_UNCAUGHT:
            case SceneActions.ACTION_SEND_SMS_YES:
                if (sc.hasEmoji()) {
                    return new StageAskAddEmoji(mSceneBase, mFeedback);
                } else {
                    return new StageSendSmsConfirm(mSceneBase, mFeedback);
                }
            case SceneActions.ACTION_SEND_SMS_NO:
            case SceneActions.ACTION_SEND_SMS_CHANGE_SMS_BODY:
                return new StageAskForSmsBody(mSceneBase, mFeedback);
            case SceneActions.ACTION_SEND_SMS_MSGBODY:
                return new StageAskForSendSmsToContact(mSceneBase, mFeedback);
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
        SmsContent sc = getSmsContent();
        Context context = mSceneBase.getContext();
        String[] uiAndTtsText = SceneUtil.getConfirmMsg(context, sc.getMessageBody());
        if (uiAndTtsText.length > 0) {
            String[] alignments = SceneUtil.getAlignmentCommon(context);
            requestAsrAlignment(alignments);
            Bundle args = new Bundle();
            String[] options = SceneUtil.getOptionsCommon(context);
            String uiText = uiAndTtsText[0];
            String ttsText = uiAndTtsText[1];
            OptionList optionList = new OptionList(OptionList.REQUEST_TYPE_TEXT);
            optionList.setTitle(uiText);
            optionList.setIconRes(SceneUtil.ICON_MSG);
            for (String option : options) {
                optionList.add(new Option(option));
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
