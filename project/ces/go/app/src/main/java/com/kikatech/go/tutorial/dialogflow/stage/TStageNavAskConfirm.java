package com.kikatech.go.tutorial.dialogflow.stage;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.kikatech.go.dialogflow.SceneUtil;
import com.kikatech.go.dialogflow.model.Option;
import com.kikatech.go.dialogflow.model.OptionList;
import com.kikatech.go.tutorial.TutorialUtil;
import com.kikatech.go.util.LogUtil;
import com.kikatech.voice.core.dialogflow.intent.Intent;
import com.kikatech.voice.core.dialogflow.scene.ISceneFeedback;
import com.kikatech.voice.core.dialogflow.scene.SceneBase;
import com.kikatech.voice.core.dialogflow.scene.SceneStage;

/**
 * @author SkeeterWang Created on 2018/5/11.
 */

class TStageNavAskConfirm extends BaseTutorialStage {
    protected final String TAG = "TStageNavAskConfirm";

    private String mAddress;

    // 1-3-4
    TStageNavAskConfirm(@NonNull SceneBase scene, ISceneFeedback feedback, String address) {
        super(scene, feedback);
        mAddress = address;
    }

    @Override
    public SceneStage next(String action, Bundle extra) {
        return super.next(action, extra);
    }

    @Override
    SceneStage getBackStage() {
        return new TStageNavAskConfirmAlert(mSceneBase, mFeedback, mAddress);
    }

    @Override
    SceneStage getNexStage(@NonNull String action, Bundle extra) {
        switch (action) {
            case Intent.ACTION_USER_INPUT:
                String userInput = Intent.parseUserInput(extra);
                if (!TextUtils.isEmpty(userInput) && isOkCommand(userInput.toLowerCase())) {
                    // goto 1-4-1
                    return new TStageNavDoneAlert(mSceneBase, mFeedback);
                } else {
                    return getBackStage();
                }
        }
        return super.onInputIncorrect();
    }

    private boolean isOkCommand(String input) {
        String[] OK_COMMANDS = new String[]{"ok", "okay"};
        for (String command : OK_COMMANDS) {
            if (command.equals(input)) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected void prepare() {
        if (LogUtil.DEBUG) {
            LogUtil.logd(TAG, "prepare");
        }
    }

    @Override
    public void doAction() {
        onStageActionStart();
        action();
    }

    @Override
    public void action() {
        if (LogUtil.DEBUG) {
            LogUtil.logd(TAG, "action");
        }
        setQueryAnyWords(true);
        Context context = mSceneBase.getContext();
        String[] uiAndTtsText = SceneUtil.getConfirmAddress(context, mAddress);
        if (uiAndTtsText.length > 0) {
            String[] options = SceneUtil.getOptionsCommon2(context);
            requestAsrAlignment(options);
            String uiText = uiAndTtsText[0];
            String ttsText = uiAndTtsText[1];
            OptionList optionList = new OptionList(OptionList.REQUEST_TYPE_TEXT);
            optionList.setTitle(uiText);
            optionList.setIconRes(SceneUtil.ICON_NAVIGATION);
            for (String option : options) {
                optionList.add(new Option(option));
            }
            Bundle args = new Bundle();
            args.putParcelable(SceneUtil.EXTRA_OPTIONS_LIST, optionList);
            speak(ttsText, args);
        }
    }

    @Override
    public void onStageActionDone(boolean isInterrupted) {
        super.onStageActionDone(isInterrupted);
        // start asr, except "OK"
        Bundle extras = new Bundle();
        extras.putInt(TutorialUtil.StageEvent.KEY_TYPE, TutorialUtil.StageActionType.ASR);
        send(extras);
    }
}
