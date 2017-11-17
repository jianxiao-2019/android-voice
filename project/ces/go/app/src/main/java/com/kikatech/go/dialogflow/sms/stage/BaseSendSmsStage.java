package com.kikatech.go.dialogflow.sms.stage;

import android.os.Bundle;
import android.support.annotation.NonNull;

import com.kikatech.go.dialogflow.sms.SceneSendSms;
import com.kikatech.go.dialogflow.sms.SendSmsUtil;
import com.kikatech.go.dialogflow.sms.SmsContent;
import com.kikatech.go.dialogflow.sms.SmsSceneActions;
import com.kikatech.go.util.LogUtil;
import com.kikatech.voice.core.dialogflow.scene.ISceneFeedback;
import com.kikatech.voice.core.dialogflow.scene.SceneBase;
import com.kikatech.voice.core.dialogflow.scene.SceneStage;

/**
 * Created by brad_chang on 2017/11/16.
 */

public class BaseSendSmsStage extends SceneStage {

    protected final String TAG = getClass().getSimpleName();

    BaseSendSmsStage(@NonNull SceneBase scene, ISceneFeedback feedback) {
        super(scene, feedback);
        if (LogUtil.DEBUG) LogUtil.log(TAG, "init");
    }

    SmsContent getSmsContent() {
        if (LogUtil.DEBUG) {
            LogUtil.log(TAG, ((SceneSendSms) mSceneBase).getSmsContent().toString());
        }
        return ((SceneSendSms) mSceneBase).getSmsContent();
    }

    @Override
    public SceneStage next(String action, Bundle extra) {
        if (LogUtil.DEBUG) LogUtil.log(TAG, "action:" + action);
        ((SceneSendSms) mSceneBase).updateSmsContent(SendSmsUtil.parseContactName(extra));
        if(action.equals(SmsSceneActions.ACTION_SEND_SMS_CANCEL)) {
            return new StageCancel(mSceneBase, mFeedback);
        }
        return getNextStage(action, extra);
    }

    protected SceneStage getNextStage(String action, Bundle extra) {
        return null;
    }

    @Override
    public void action() {
        if (LogUtil.DEBUG) {
            LogUtil.log(TAG, "action : do nothing");
        }
    }

    /**
     * SendSMS 2.7 檢查是否有 SMS 內容
     */
    static SceneStage getStageCheckSmsBody(String TAG, SmsContent sc, SceneBase mSceneBase, ISceneFeedback mFeedback) {
        if (LogUtil.DEBUG) LogUtil.log(TAG, "Sms Body : " + sc.getSmsBody());
        if (sc.isSmsBodyAvailable()) {
            // SendSMS 2.9 確認訊息內容
            return new StageAskForSendSmsToContact(mSceneBase, mFeedback);
        } else {
            // SendSMS 2.8 詢問 SMS 內容
            return new StageAskForSmsBody(mSceneBase, mFeedback);
        }
    }

    /**
     * SendSMS 2.6.1 檢查用戶號碼選擇
     */
//    static SceneStage getStageCheckNumberChoice(String TAG, SmsContent sc, SceneBase mSceneBase, ISceneFeedback mFeedback) {
//        if (LogUtil.DEBUG) LogUtil.log(TAG, "Number count : " + sc.getNumberCount());
//        boolean isChoosed = true;
//        if(isChoosed) {
//            return getStageCheckSmsBody(TAG, sc, mSceneBase, mFeedback);
//        } else {
//            return null;
//        }
//    }

    /**
     * SendSMS 2.5 檢查是否有兩組電話以上
     */
    static SceneStage getStageCheckNumberCount(String TAG, SmsContent sc, SceneBase mSceneBase, ISceneFeedback mFeedback) {
        if (LogUtil.DEBUG) LogUtil.log(TAG, "Tel Number count : " + sc.getNumberCount());
        if (sc.getNumberCount() == 1) {
            sc.setChoosedNumber(sc.getPhoneNumbers().get(0));
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
        if (sc.isContactMatched(mSceneBase.getContext())) {
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