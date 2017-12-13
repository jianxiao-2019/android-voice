package com.kikatech.go.dialogflow.im.reply.stage;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Pair;

import com.kikatech.go.dialogflow.SceneUtil;
import com.kikatech.go.dialogflow.UserSettings;
import com.kikatech.go.dialogflow.im.reply.SceneActions;
import com.kikatech.go.dialogflow.model.UserMsg;
import com.kikatech.go.message.im.BaseIMObject;
import com.kikatech.go.util.LogUtil;
import com.kikatech.voice.core.dialogflow.scene.ISceneFeedback;
import com.kikatech.voice.core.dialogflow.scene.SceneBase;
import com.kikatech.voice.core.dialogflow.scene.SceneStage;
import com.kikatech.voice.core.tts.TtsSource;

/**
 * Created by brad_chang on 2017/11/28.
 */

public class ReadContentAndAskToReplyImReplyIMStage extends BaseReplyIMStage {

    ReadContentAndAskToReplyImReplyIMStage(@NonNull SceneBase scene, ISceneFeedback feedback) {
        super(scene, feedback);
    }

    @Override
    public SceneStage getNextStage(String action, Bundle extra) {
        switch (action) {
            case SceneActions.ACTION_REPLY_IM_YES:
                return new AskMsgBodyReplyImReplyIMStage(mSceneBase, mFeedback);
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
        BaseIMObject imObject = getReplyMessage().getIMObject();
        if (LogUtil.DEBUG) {
            LogUtil.log(TAG, imObject.getUserName() + " said: " + imObject.getMsgContent() + ", do you want to reply ?");
        }
        String[] uiAndTtsText;
        switch (UserSettings.getReplyMessageSetting()) {
            case UserSettings.SETTING_REPLY_SMS_READ:
                uiAndTtsText = SceneUtil.getReadMsgDirectly(mSceneBase.getContext(), imObject.getUserName(), imObject.getMsgContent());
                break;
            default:
            case UserSettings.SETTING_REPLY_SMS_ASK_USER:
                uiAndTtsText = SceneUtil.getReadMsg(mSceneBase.getContext(), imObject.getUserName(), imObject.getMsgContent());
                break;
        }
        if (uiAndTtsText.length > 0) {
            String uiText = uiAndTtsText[0];
            String ttsPart1 = uiAndTtsText[1];
            String ttsPart2 = uiAndTtsText[2];

            Bundle args = new Bundle();

            UserMsg userMsg = new UserMsg(imObject.getAvatarFilePath(), imObject.getUserName(), imObject.getAppInfo(), imObject.getMsgContent());
            userMsg.setIconRes(SceneUtil.ICON_MSG);
            args.putParcelable(SceneUtil.EXTRA_USR_MSG, userMsg);
            args.putString(SceneUtil.EXTRA_UI_TEXT, uiText);

            Pair<String, Integer>[] pairs = new Pair[2];
            pairs[0] = new Pair<>(ttsPart1, TtsSource.TTS_SPEAKER_1);
            pairs[1] = new Pair<>(ttsPart2, TtsSource.TTS_SPEAKER_2);
            speak(pairs, args);
        }
    }

    @Override
    public void onStageActionDone(boolean isInterrupted) {
        mSceneBase.nextStage(new AskToReplyImOptionReplyIMStage(mSceneBase, mFeedback));
    }
}
