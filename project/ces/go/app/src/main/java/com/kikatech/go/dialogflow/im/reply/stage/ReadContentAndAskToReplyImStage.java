package com.kikatech.go.dialogflow.im.reply.stage;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Pair;

import com.kikatech.go.dialogflow.SceneUtil;
import com.kikatech.go.dialogflow.UserSettings;
import com.kikatech.go.dialogflow.im.reply.SceneActions;
import com.kikatech.go.message.im.BaseIMObject;
import com.kikatech.go.util.LogUtil;
import com.kikatech.voice.core.dialogflow.scene.ISceneFeedback;
import com.kikatech.voice.core.dialogflow.scene.SceneBase;
import com.kikatech.voice.core.dialogflow.scene.SceneStage;
import com.kikatech.voice.core.tts.TtsSpeaker;

/**
 * Created by brad_chang on 2017/11/28.
 */

public class ReadContentAndAskToReplyImStage extends BaseStage {

    private final BaseIMObject mIMObject;
    ReadContentAndAskToReplyImStage(@NonNull SceneBase scene, ISceneFeedback feedback, BaseIMObject imo) {
        super(scene, feedback);
        mIMObject = imo;
    }

    @Override
    public SceneStage getNextStage(String action, Bundle extra) {
        switch (action) {
            case SceneActions.ACTION_REPLY_IM_YES:
                return new AskMsgBodyReplyImStage(mSceneBase, mFeedback, mIMObject);
            case SceneActions.ACTION_REPLY_IM_NO:
            case SceneActions.ACTION_REPLY_IM_CANCEL:
                if (LogUtil.DEBUG) LogUtil.log(TAG, "Stop !!");
                exitScene();
                break;
        }
        return this;
    }

    @Override
    public void doAction() {
        action();
    }

    @Override
    public void action() {
        if(LogUtil.DEBUG) {
            LogUtil.log(TAG, mIMObject.getUserName() + " said: " + mIMObject.getMsgContent() + ", do you want to reply ?");
        }
        String[] uiAndTtsText;
        switch (UserSettings.getReplyMessageSetting()) {
            case UserSettings.SETTING_REPLY_SMS_READ:
                uiAndTtsText = SceneUtil.getReadMsgDirectly(mSceneBase.getContext(), mIMObject.getUserName(), mIMObject.getMsgContent());
                break;
            default:
            case UserSettings.SETTING_REPLY_SMS_ASK_USER:
                uiAndTtsText = SceneUtil.getReadMsg(mSceneBase.getContext(), mIMObject.getUserName(), mIMObject.getMsgContent());
                break;
        }
        if (uiAndTtsText.length > 0) {
            String uiText = uiAndTtsText[0];
            String ttsPart1 = uiAndTtsText[1];
            String ttsPart2 = uiAndTtsText[2];
            Bundle args = new Bundle();
            args.putString(SceneUtil.EXTRA_UI_TEXT, uiText);

            Pair<String, Integer>[] pairs = new Pair[2];
            pairs[0] = new Pair<>(ttsPart1, TtsSpeaker.TTS_VOICE_1);
            pairs[1] = new Pair<>(ttsPart2, TtsSpeaker.TTS_VOICE_2);
            speak(pairs, args);
        }
    }

    @Override
    public void onStageActionDone(boolean isInterrupted) {
        mSceneBase.nextStage(new AskToReplyImOptionStage(mSceneBase, mFeedback, mIMObject));
        super.onStageActionDone(isInterrupted);
    }
}
