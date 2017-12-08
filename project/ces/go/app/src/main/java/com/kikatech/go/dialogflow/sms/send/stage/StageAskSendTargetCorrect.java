package com.kikatech.go.dialogflow.sms.send.stage;

import android.os.Bundle;
import android.support.annotation.NonNull;

import com.kikatech.go.dialogflow.SceneUtil;
import com.kikatech.go.dialogflow.sms.SmsContent;
import com.kikatech.go.dialogflow.sms.SmsUtil;
import com.kikatech.go.dialogflow.sms.send.SceneActions;
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
    protected SceneStage getNextStage(String action, Bundle extra) {
        SmsContent sc = getSmsContent();
        switch (action) {
            case SceneActions.ACTION_SEND_SMS_NO:

                SmsContent.IntentContent ic = SmsUtil.parseContactName(extra);
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
        action();
    }

    @Override
    public void action() {
        mCurrentName = getSmsContent().getMatchedName();
        String[] uiAndTtsText = SceneUtil.getConfirmContact(mSceneBase.getContext(), mCurrentName);
        if (uiAndTtsText.length > 0) {
            String uiText = uiAndTtsText[0];
            String ttsText = uiAndTtsText[1];
            Bundle args = new Bundle();
            args.putString(SceneUtil.EXTRA_UI_TEXT, uiText);
            speak(ttsText, args);
        }
    }
}