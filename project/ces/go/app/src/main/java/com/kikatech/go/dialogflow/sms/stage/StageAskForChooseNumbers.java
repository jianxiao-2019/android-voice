package com.kikatech.go.dialogflow.sms.stage;

import android.os.Bundle;
import android.support.annotation.NonNull;

import com.kikatech.go.dialogflow.sms.SmsSceneActions;
import com.kikatech.go.util.LogUtil;
import com.kikatech.voice.core.dialogflow.scene.ISceneFeedback;
import com.kikatech.voice.core.dialogflow.scene.SceneBase;
import com.kikatech.voice.core.dialogflow.scene.SceneStage;

import java.util.List;

/**
 * Created by brad_chang on 2017/11/15.
 */

public class StageAskForChooseNumbers extends BaseSendSmsStage {

    /**
     * SendSMS 2.6 向用戶進一步確認號碼或識別標籤
     */
    StageAskForChooseNumbers(@NonNull SceneBase scene, ISceneFeedback feedback) {
        super(scene, feedback);
    }

    @Override
    protected SceneStage getNextStage(String action, Bundle extra) {
        if(!action.equals(SmsSceneActions.ACTION_SEND_SMS_SELECT_NUM)) {
            if (LogUtil.DEBUG) LogUtil.log(TAG, "Unsupported action:" + action);
            return null;
        }
        return getStageCheckSmsBody(TAG, getSmsContent(), mSceneBase, mFeedback);
    }

    @Override
    public void action() {
        List<String> numbers = getSmsContent().getPhoneNumbers();
        if(numbers.size() > 1) {
            speak("2.6 Choose a number from following list. First " + numbers.get(0) + ", or second " + numbers.get(1));
        } else {
            speak("2.6 Error, only one phone number");
        }
    }
}
