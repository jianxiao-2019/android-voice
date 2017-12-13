package com.kikatech.go.dialogflow.sms.send.stage;

import android.os.Bundle;
import android.support.annotation.NonNull;

import com.kikatech.go.dialogflow.BaseSceneStage;
import com.kikatech.go.dialogflow.sms.SmsUtil;
import com.kikatech.go.dialogflow.sms.send.SceneSendSms;
import com.kikatech.go.dialogflow.sms.SmsContent;
import com.kikatech.go.dialogflow.sms.send.SceneActions;
import com.kikatech.go.util.LogUtil;
import com.kikatech.voice.core.dialogflow.intent.Intent;
import com.kikatech.voice.core.dialogflow.scene.ISceneFeedback;
import com.kikatech.voice.core.dialogflow.scene.SceneBase;
import com.kikatech.voice.core.dialogflow.scene.SceneStage;
import com.kikatech.voice.util.contact.ContactManager;

/**
 * Created by brad_chang on 2017/11/16.
 */

public class BaseSendSmsStage extends BaseSceneStage {

    BaseSendSmsStage(@NonNull SceneBase scene, ISceneFeedback feedback) {
        super(scene, feedback);
    }

    SmsContent getSmsContent() {
        if (LogUtil.DEBUG) {
            LogUtil.log(TAG, ((SceneSendSms) mSceneBase).getSmsContent().toString());
        }
        return ((SceneSendSms) mSceneBase).getSmsContent();
    }

    protected int getAnyTAgParseTarget(String action) {
        int tagAnyTarget = SmsUtil.TAG_ANY_STAND_FOR_MSG_BODY;
        if (action.equals(SceneActions.ACTION_SEND_SMS_NO)) {
            tagAnyTarget = SmsUtil.TAG_ANY_STAND_FOR_NAME;
        } else if (action.equals(Intent.ACTION_USER_INPUT)) {
            tagAnyTarget = SmsUtil.TAG_ANY_STAND_FOR_USER_INPUT;
        }
        return tagAnyTarget;
    }

    @Override
    public SceneStage next(String action, Bundle extra) {
        if (LogUtil.DEBUG) LogUtil.log(TAG, "action:" + action);
        if (action.equals(Intent.ACTION_RCMD_EMOJI)) {
            String emojiJson = Intent.parseEmojiJsonString(extra);
            ((SceneSendSms) mSceneBase).updateEmoji(emojiJson);
        } else if (action.equals(SceneActions.ACTION_SEND_SMS_CANCEL)) {
            return new StageCancel(mSceneBase, mFeedback);
        } else {
            int tagAnyTarget = getAnyTAgParseTarget(action);
            ((SceneSendSms) mSceneBase).updateSmsContent(SmsUtil.parseContactName(extra, tagAnyTarget));
        }
        return getNextStage(action, extra);
    }

    protected SceneStage getNextStage(String aStageAskSendTargetCorrecttion, Bundle extra) {
        return null;
    }

    @Override
    public void prepare() {
    }

    @Override
    public void action() {
        if (LogUtil.DEBUG) {
            LogUtil.logw(TAG, "action : do nothing");
        }
    }

    /**
     * SendSMS 2.7 檢查是否有 SMS 內容
     */
    static SceneStage getStageCheckSmsBody(String TAG, SmsContent sc, SceneBase mSceneBase, ISceneFeedback mFeedback) {
        if (LogUtil.DEBUG) LogUtil.log(TAG, "Sms Body : " + sc.getMessageBody());
        if (sc.isSmsBodyAvailable()) {
            // SendSMS 2.9 確認訊息內容
            return new StageAskForSendSmsToContact(mSceneBase, mFeedback);
        } else {
            // SendSMS 2.8 詢問 SMS 內容
            return new StageAskForSmsBody(mSceneBase, mFeedback);
        }
    }

    /**
     * SendSMS 2.5 檢查是否有兩組電話以上
     */
    static SceneStage getStageCheckNumberCount(String TAG, SmsContent sc, SceneBase mSceneBase, ISceneFeedback mFeedback) {
        if (LogUtil.DEBUG) LogUtil.log(TAG, "Tel Number count : " + sc.getNumberCount());
        if (sc.getNumberCount() == 1) {
            sc.setChosenNumber(sc.getPhoneNumbers().get(0).number);
            return getStageCheckSmsBody(TAG, sc, mSceneBase, mFeedback);
        } else {
            // SendSMS 2.6 向用戶進一步確認號碼或識別標籤
            return new StageAskForChooseNumbers(mSceneBase, mFeedback);
        }
    }

    /**
     * SendSMS 2.2 檢查是否找到匹配的聯絡人
     */
    static SceneStage getStageCheckContactMatched(String TAG, SmsContent sc, SceneBase mSceneBase, ISceneFeedback mFeedback) {
        ContactManager.MatchedContact matchedContact = sc.isContactMatched(mSceneBase.getContext());
        if (matchedContact != null) {
            if (LogUtil.DEBUG)
                LogUtil.log(TAG, "Contact Matched !! Matched name : " + sc.getMatchedName());
            if (sc.isSimilarContact()) {
                // SendSMS 2.4 確認傳訊對象
                if (LogUtil.DEBUG) LogUtil.log(TAG, "Similar contact found !");
                return new StageAskSendTargetCorrect(mSceneBase, mFeedback);
            } else {
                return getStageCheckNumberCount(TAG, sc, mSceneBase, mFeedback);
            }
        } else {
            // SendSMS 2.3 再次詢問對象
            if (LogUtil.DEBUG) LogUtil.log(TAG, "Cannot find contact, ask again");
            return new StageAskForSendTarget(mSceneBase, mFeedback);
        }
    }

    /**
     * SendSMS 2.0 檢查是否有傳送對象
     */
    static SceneStage checkSendTargetAvailable(String TAG, SmsContent sc, SceneBase mSceneBase, ISceneFeedback mFeedback) {
        if (!sc.isContactAvailable()) {
            // SendSMS 2.1 詢問傳送對象
            return new StageAskForSendTarget(mSceneBase, mFeedback);
        } else {
            return getStageCheckContactMatched(TAG, sc, mSceneBase, mFeedback);
        }
    }
}