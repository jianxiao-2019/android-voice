package com.kikatech.go.dialogflow.sms.reply.stage;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Pair;

import com.kikatech.go.dialogflow.SceneUtil;
import com.kikatech.go.dialogflow.UserSettings;
import com.kikatech.go.dialogflow.model.UserMsg;
import com.kikatech.go.dialogflow.sms.reply.SceneActions;
import com.kikatech.go.message.sms.SmsObject;
import com.kikatech.go.util.AppInfo;
import com.kikatech.go.util.LogUtil;
import com.kikatech.voice.core.dialogflow.scene.ISceneFeedback;
import com.kikatech.voice.core.dialogflow.scene.SceneBase;
import com.kikatech.voice.core.dialogflow.scene.SceneStage;
import com.kikatech.voice.core.tts.TtsSpeaker;

/**
 * Created by brad_chang on 2017/11/20.
 */

public class AskToReplySmsReadStage extends BaseReplySmsStage {

    AskToReplySmsReadStage(@NonNull SceneBase scene, ISceneFeedback feedback) {
        super(scene, feedback);
    }

    @Override
    public SceneStage getNextStage(String action, Bundle extra) {
        switch (action) {
            case SceneActions.ACTION_REPLY_SMS_YES:
                return new AskMsgBodyReplySmsStage(mSceneBase, mFeedback);
            case SceneActions.ACTION_REPLY_SMS_NO:
            case SceneActions.ACTION_REPLY_SMS_CANCEL:
                if (LogUtil.DEBUG) LogUtil.log(TAG, "Stop !!");
                exitScene();
                return null;
        }
        return this;
    }

    @Override
    public void doAction() {
        action();
    }

    @Override
    public void action() {
        SmsObject smsObject = getReplyMessage().getSmsObject();
        String[] uiAndTtsText;
        switch (UserSettings.getReplyMessageSetting()) {
            case UserSettings.SETTING_REPLY_SMS_READ:
                uiAndTtsText = SceneUtil.getReadMsgDirectly(mSceneBase.getContext(), smsObject.getUserName(), smsObject.getMsgContent());
                break;
            default:
            case UserSettings.SETTING_REPLY_SMS_ASK_USER:
                uiAndTtsText = SceneUtil.getReadMsg(mSceneBase.getContext(), smsObject.getUserName(), smsObject.getMsgContent());
                break;
        }
        if (uiAndTtsText.length > 0) {
            String uiText = uiAndTtsText[0];
            String ttsPart1 = uiAndTtsText[1];
            String ttsPart2 = uiAndTtsText[2];

            Bundle args = new Bundle();

            UserMsg userMsg = new UserMsg(smsObject.getPhotoUri(), smsObject.getUserName(), AppInfo.SMS, smsObject.getMsgContent());
            args.putParcelable(SceneUtil.EXTRA_USR_MSG, userMsg);
            args.putString(SceneUtil.EXTRA_UI_TEXT, uiText);

            Pair<String, Integer>[] pairs = new Pair[2];
            pairs[0] = new Pair<>(ttsPart1, TtsSpeaker.TTS_VOICE_1);
            pairs[1] = new Pair<>(ttsPart2, TtsSpeaker.TTS_VOICE_2);
            speak(pairs, args);
        }
    }

    @Override
    public void onStageActionDone(boolean isInterrupted) {
        mSceneBase.nextStage(new AskToReplySmsOptionStage(mSceneBase, mFeedback));
    }
}
