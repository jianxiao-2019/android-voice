package com.kikatech.go.dialogflow.im.reply.stage;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;

import com.kikatech.go.dialogflow.SceneUtil;
import com.kikatech.go.dialogflow.im.reply.SceneActions;
import com.kikatech.go.dialogflow.model.Option;
import com.kikatech.go.dialogflow.model.OptionList;
import com.kikatech.go.util.LogUtil;
import com.kikatech.voice.core.dialogflow.scene.ISceneFeedback;
import com.kikatech.voice.core.dialogflow.scene.SceneBase;
import com.kikatech.voice.core.dialogflow.scene.SceneStage;

/**
 * Created by brad_chang on 2017/11/28.
 */

public class AskToReplyImOptionReplyIMStage extends BaseReplyIMStage {

    AskToReplyImOptionReplyIMStage(@NonNull SceneBase scene, ISceneFeedback feedback) {
        super(scene, feedback);
    }

    @Override
    public SceneStage getNextStage(String action, Bundle extra) {
        cancelAsrAlignment();
        switch (action) {
            case SceneActions.ACTION_REPLY_IM_YES:
                return new AskMsgBodyReplyImReplyIMStage(mSceneBase, mFeedback);
            case SceneActions.ACTION_REPLY_IM_NO:
            case SceneActions.ACTION_REPLY_IM_CANCEL:
                if (LogUtil.DEBUG) LogUtil.log(TAG, "Stop !!");
                exitScene();
                break;
        }
        return null;
    }

    @Override
    public void doAction() {
        onStageActionStart();
        action();
    }

    @Override
    public void action() {
        Context context = mSceneBase.getContext();
        String[] uiAndTtsText = SceneUtil.getAskReplyMsg(context);
        if (uiAndTtsText.length > 0) {
            String[] alignments = SceneUtil.getAlignmentCommon(context);
            requestAsrAlignment(alignments);
            Bundle args = new Bundle();
            String[] options = SceneUtil.getOptionsCommon(context);
            String uiText = uiAndTtsText[0];
            String ttsText = uiAndTtsText[1];
            OptionList optionList = new OptionList(OptionList.REQUEST_TYPE_TEXT);
            optionList.setTitle(uiText);
            optionList.setIconRes(SceneUtil.ICON_MSG);
            for (String option : options) {
                optionList.add(new Option(option));
            }
            args.putParcelable(SceneUtil.EXTRA_OPTIONS_LIST, optionList);
            speak(ttsText, args);
        }
    }
}
