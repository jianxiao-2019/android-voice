package com.kikatech.go.dialogflow.sms.send.stage;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;

import com.kikatech.go.dialogflow.AsrConfigUtil;
import com.kikatech.go.dialogflow.SceneUtil;
import com.kikatech.go.dialogflow.model.ContactOptionList;
import com.kikatech.go.dialogflow.model.Option;
import com.kikatech.go.dialogflow.model.OptionList;
import com.kikatech.go.dialogflow.model.TtsText;
import com.kikatech.go.dialogflow.sms.SmsContent;
import com.kikatech.go.dialogflow.sms.SmsUtil;
import com.kikatech.go.dialogflow.sms.send.SceneActions;
import com.kikatech.go.util.AppInfo;
import com.kikatech.voice.core.dialogflow.scene.ISceneFeedback;
import com.kikatech.voice.core.dialogflow.scene.SceneBase;
import com.kikatech.voice.core.dialogflow.scene.SceneStage;
import com.kikatech.voice.util.contact.ContactManager;

/**
 * Created by brad_chang on 2017/11/15.
 */

public class StageAskSendTargetCorrect extends BaseSendSmsStage {

    /**
     * SendSMS 2.4 確認傳訊對象
     */
    private String mCurrentName = "";

    StageAskSendTargetCorrect(@NonNull SceneBase scene, ISceneFeedback feedback) {
        super(scene, feedback);
    }

    @Override
    protected @AsrConfigUtil.ASRMode
    int getAsrMode() {
        return AsrConfigUtil.ASR_MODE_CONVERSATION_CMD_ALTER;
    }

    @Override
    protected SceneStage getNextStage(String action, Bundle extra) {
        cancelAsrAlignment();
        SmsContent sc = getSmsContent();
        switch (action) {
            case SceneActions.ACTION_SEND_SMS_NO:

                SmsContent.IntentContent ic = SmsUtil.parseSmsContent(extra);
                if (ic.isNameEmpty()) {
                    sc.setIntentContent(ic);
                    return new StageAskForSendTarget(mSceneBase, mFeedback);
                }

                ContactManager.MatchedContact matchedContact = sc.isContactMatched(mSceneBase.getContext());
                if (matchedContact != null) {
                    if (mCurrentName.equals(sc.getMatchedName())) {
                        return getStageCheckNumberCount(TAG, sc, mSceneBase, mFeedback);
                    } else {
                        return new StageAskSendTargetCorrect(mSceneBase, mFeedback);
                    }
                } else {
                    return new StageAskForSendTarget(mSceneBase, mFeedback);
                }

            case SceneActions.ACTION_SEND_SMS_YES:
                return getStageCheckNumberCount(TAG, sc, mSceneBase, mFeedback);
            default:
                return this;
        }
    }

    @Override
    public void doAction() {
        onStageActionStart();
        action();
    }

    @Override
    public void action() {
        Context context = mSceneBase.getContext();
        SmsContent smsContent = getSmsContent();
        mCurrentName = smsContent.getMatchedName();
        String photoUri = smsContent.getMatchedAvatar();
        String[] uiAndTtsText = SceneUtil.getConfirmContact(context, mCurrentName);
        if (uiAndTtsText.length > 0) {
            String[] alignments = SceneUtil.getAlignmentCommon(context);
            requestAsrAlignment(alignments);
            String[] options = SceneUtil.getOptionsCommon(context);
            String uiText = uiAndTtsText[0];
            String ttsText = uiAndTtsText[1];
            ContactOptionList contactOptionList = new ContactOptionList(OptionList.REQUEST_TYPE_TEXT);
            contactOptionList.setTitle(uiText);
            contactOptionList.setAvatar(photoUri);
            contactOptionList.setAppInfo(AppInfo.SMS);
            contactOptionList.setIconRes(SceneUtil.ICON_MSG);
            for (String option : options) {
                contactOptionList.add(new Option(option));
            }
            Bundle args = new Bundle();
            args.putParcelable(SceneUtil.EXTRA_CONTACT_OPTIONS_LIST, contactOptionList);
            speak(ttsText, args);
        }
    }
}