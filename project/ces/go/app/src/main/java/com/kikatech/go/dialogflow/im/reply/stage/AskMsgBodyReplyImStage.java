package com.kikatech.go.dialogflow.im.reply.stage;

import android.os.Bundle;
import android.support.annotation.NonNull;

import com.kikatech.go.dialogflow.SceneUtil;
import com.kikatech.go.dialogflow.im.reply.SceneActions;
import com.kikatech.go.dialogflow.model.TtsText;
import com.kikatech.go.dialogflow.sms.SmsUtil;
import com.kikatech.go.message.im.BaseIMObject;
import com.kikatech.voice.core.dialogflow.scene.ISceneFeedback;
import com.kikatech.voice.core.dialogflow.scene.SceneBase;
import com.kikatech.voice.core.dialogflow.scene.SceneStage;

/**
 * Created by brad_chang on 2017/11/28.
 */

public class AskMsgBodyReplyImStage extends BaseStage {

    AskMsgBodyReplyImStage(@NonNull SceneBase scene, ISceneFeedback feedback) {
        super(scene, feedback);
    }

    @Override
    public SceneStage getNextStage(String action, Bundle extra) {
        setQueryAnyWords(false);
        if(action.equals(SceneActions.ACTION_REPLY_IM_MSG_BODY)) {
            String messageBody = SmsUtil.parseTagAny(extra);
            getReplyMessage().updateMsgBody(messageBody);
            return new ConfirmMsgBodyReplyImStage(mSceneBase, mFeedback);
        }
        return this;
    }

    @Override
    public void doAction() {
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
