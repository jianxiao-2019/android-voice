package com.kikatech.go.dialogflow.sms.send.stage;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.kikatech.go.dialogflow.SceneUtil;
import com.kikatech.go.dialogflow.sms.SmsContent;
import com.kikatech.go.dialogflow.sms.SmsUtil;
import com.kikatech.go.dialogflow.sms.send.SceneActions;
import com.kikatech.go.util.LogUtil;
import com.kikatech.voice.core.dialogflow.intent.Intent;
import com.kikatech.voice.core.dialogflow.scene.ISceneFeedback;
import com.kikatech.voice.core.dialogflow.scene.SceneBase;
import com.kikatech.voice.core.dialogflow.scene.SceneStage;

/**
 * Created by brad_chang on 2017/11/15.
 */

public class StageAskForSendTarget extends BaseSendSmsStage {
    /**
     * SendSMS 2.1 詢問傳送對象 / 2.3 再次詢問對象
     */
    StageAskForSendTarget(@NonNull SceneBase scene, ISceneFeedback feedback) {
        super(scene, feedback);
    }

    @Override
    protected int getAnyTAgParseTarget(String action) {
        return SmsUtil.TAG_ANY_STAND_FOR_NAME;
    }

    @Override
    protected SceneStage getNextStage(String action, Bundle extra) {
        setQueryAnyWords(false);
        SmsContent sc = getSmsContent();
        String[] userSay = Intent.parseUserInputNBest(extra);
        if (userSay != null && userSay.length != 0) {
            sc.updateNames(userSay);
        }
        return getStageCheckContactMatched(TAG, sc, mSceneBase, mFeedback);
    }

    @Override
    public void doAction() {
        action();
    }

    @Override
    public void action() {
        setQueryAnyWords(true);

        SmsContent sc = getSmsContent();
        String[] uiAndTtsText;
        if (sc.isContactAvailable()) {
            // SendSMS 2.3 再次詢問對象
            uiAndTtsText = SceneUtil.getContactNotFound(mSceneBase.getContext());
        } else {
            // SendSMS 2.1 詢問傳送對象
            uiAndTtsText = SceneUtil.getAskContact(mSceneBase.getContext());
        }
        if (uiAndTtsText.length > 0) {
            String uiText = uiAndTtsText[0];
            String ttsText = uiAndTtsText[1];
            Bundle args = new Bundle();
            args.putString(SceneUtil.EXTRA_UI_TEXT, uiText);
            speak(ttsText, args);
        }
    }
}