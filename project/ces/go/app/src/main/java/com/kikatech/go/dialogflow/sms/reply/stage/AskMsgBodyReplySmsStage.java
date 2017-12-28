package com.kikatech.go.dialogflow.sms.reply.stage;

import android.os.Bundle;
import android.support.annotation.NonNull;

import com.kikatech.go.dialogflow.AsrConfigUtil;
import com.kikatech.go.dialogflow.SceneUtil;
import com.kikatech.go.dialogflow.model.TtsText;
import com.kikatech.go.dialogflow.sms.SmsUtil;
import com.kikatech.go.dialogflow.sms.reply.SceneActions;
import com.kikatech.go.message.sms.SmsObject;
import com.kikatech.go.util.LogUtil;
import com.kikatech.voice.core.dialogflow.scene.ISceneFeedback;
import com.kikatech.voice.core.dialogflow.scene.SceneBase;
import com.kikatech.voice.core.dialogflow.scene.SceneStage;

/**
 * Created by brad_chang on 2017/11/20.
 */

public class AskMsgBodyReplySmsStage extends BaseReplySmsStage {

    AskMsgBodyReplySmsStage(@NonNull SceneBase scene, ISceneFeedback feedback) {
        super(scene, feedback);
    }

    @Override
    protected @AsrConfigUtil.ASRMode int getAsrMode() {
        return AsrConfigUtil.ASR_MODE_CONVERSATION;
    }

    @Override
    public SceneStage getNextStage(String action, Bundle extra) {
        setQueryAnyWords(false);
        if(action.equals(SceneActions.ACTION_REPLY_SMS_MSG_BODY)) {
            String messageBody = SmsUtil.parseTagMessageBody(extra);
            getReplyMessage().updateMsgBody(messageBody);
            return new ConfirmMsgBodyReplySmsStage(mSceneBase, mFeedback);
        }
        return this;
    }

    @Override
    public void doAction() {
        onStageActionStart();
        action();
    }

    @Override
    public void action() {
        setQueryAnyWords(true);
        String[] uiAndTtsText = SceneUtil.getAskMsg(mSceneBase.getContext());
        if (uiAndTtsText.length > 0) {
            String uiText = uiAndTtsText[0];
            String ttsText = uiAndTtsText[1];
            TtsText tText = new TtsText(SceneUtil.ICON_MSG, uiText);
            Bundle args = new Bundle();
            args.putParcelable(SceneUtil.EXTRA_TTS_TEXT, tText);
            speak(ttsText, args);
        }
    }
}
