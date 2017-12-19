package com.kikatech.go.dialogflow.sms.send.stage;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;

import com.kikatech.go.dialogflow.SceneUtil;
import com.kikatech.go.dialogflow.model.Option;
import com.kikatech.go.dialogflow.model.OptionList;
import com.kikatech.go.dialogflow.sms.send.SceneActions;
import com.kikatech.go.util.LogUtil;
import com.kikatech.voice.core.dialogflow.scene.ISceneFeedback;
import com.kikatech.voice.core.dialogflow.scene.SceneBase;
import com.kikatech.voice.core.dialogflow.scene.SceneStage;

/**
 * Created by brad_chang on 2017/12/5.
 */

public class StageAskAddEmoji extends BaseSendSmsStage {
    StageAskAddEmoji(@NonNull SceneBase scene, ISceneFeedback feedback) {
        super(scene, feedback);
    }

    @Override
    protected SceneStage getNextStage(String action, Bundle extra) {
        switch (action) {
            case SceneActions.ACTION_SEND_SMS_YES:
                getSmsContent().setSendWithEmoji(true);
                return new StageSendSmsConfirm(mSceneBase, mFeedback);
            case SceneActions.ACTION_SEND_SMS_NO:
                getSmsContent().setSendWithEmoji(false);
                return new StageSendSmsConfirm(mSceneBase, mFeedback);
            default:
                if (LogUtil.DEBUG) LogUtil.log(TAG, "Unsupported action:" + action);
                getSmsContent().setSendWithEmoji(false);
                return new StageSendSmsConfirm(mSceneBase, mFeedback);
        }
    }

    @Override
    public void doAction() {
        onStageActionStart();
        action();
    }

    @Override
    public void action() {
        Context context = mSceneBase.getContext();
        String[] uiAndTtsText = SceneUtil.getAskEmoji(context, getSmsContent().getEmojiUnicode());
        if (uiAndTtsText.length > 0) {
            Bundle args = new Bundle();
            String[] options = SceneUtil.getOptionsCommon(context);
            String uiText = uiAndTtsText[0];
            String ttsText = uiAndTtsText[1];
            OptionList optionList = new OptionList(OptionList.REQUEST_TYPE_TEXT);
            optionList.setTitle(uiText);
            optionList.setIconRes(SceneUtil.ICON_MSG);
            for (String option : options) {
                optionList.add(new Option(option, null));
            }
            args.putParcelable(SceneUtil.EXTRA_OPTIONS_LIST, optionList);
            speak(ttsText, args);
        }
    }
}