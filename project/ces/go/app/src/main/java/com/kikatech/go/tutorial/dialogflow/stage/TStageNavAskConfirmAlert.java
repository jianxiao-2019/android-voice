package com.kikatech.go.tutorial.dialogflow.stage;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;

import com.kikatech.go.dialogflow.SceneUtil;
import com.kikatech.go.dialogflow.model.Option;
import com.kikatech.go.dialogflow.model.OptionList;
import com.kikatech.go.tutorial.TutorialUtil;
import com.kikatech.go.tutorial.dialogflow.SceneTutorial;
import com.kikatech.go.tutorial.dialogflow.TutorialSceneActions;
import com.kikatech.go.util.LogUtil;
import com.kikatech.voice.core.dialogflow.scene.ISceneFeedback;
import com.kikatech.voice.core.dialogflow.scene.SceneBase;
import com.kikatech.voice.core.dialogflow.scene.SceneStage;

/**
 * @author SkeeterWang Created on 2018/5/11.
 */

class TStageNavAskConfirmAlert extends BaseTutorialStage {
    protected final String TAG = "TStageNavAskConfirmAlert";

    private String mAddress;

    // 1-3-3
    TStageNavAskConfirmAlert(@NonNull SceneBase scene, ISceneFeedback feedback, String address) {
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
            case TutorialSceneActions.ACTION_NAV_ASK_CONFIRM:
                // 1-3-4
                return new TStageNavAskConfirm(mSceneBase, mFeedback, mAddress);
        }
        return super.onInputIncorrect();
    }

    @Override
    protected void prepare() {
        if (LogUtil.DEBUG) {
            LogUtil.logd(TAG, "prepare");
        }
    }

    @Override
    protected void action() {
        if (LogUtil.DEBUG) {
            LogUtil.logd(TAG, "action");
        }
        Context context = mSceneBase.getContext();
        String[] uiAndTtsText = SceneUtil.getConfirmAddress(context, mAddress);
        String[] options = SceneUtil.getOptionsCommon2(context);
        String uiText = uiAndTtsText[0];
        OptionList optionList = new OptionList(OptionList.REQUEST_TYPE_TEXT);
        optionList.setTitle(uiText);
        optionList.setIconRes(SceneUtil.ICON_NAVIGATION);
        for (String option : options) {
            optionList.add(new Option(option));
        }

        // alert
        // Say "OK" after the beep if it is correct
        String[] content = TutorialUtil.getAskConfirm(mSceneBase.getContext());
        Bundle extras = new Bundle();
        extras.putInt(TutorialUtil.StageEvent.KEY_TYPE, TutorialUtil.StageActionType.ALERT);
        extras.putParcelable(TutorialUtil.StageEvent.KEY_OPTION_LIST, optionList);
        extras.putInt(TutorialUtil.StageEvent.KEY_UI_INDEX, 4);
        extras.putString(TutorialUtil.StageEvent.KEY_UI_TITLE, content[0]);
        extras.putString(TutorialUtil.StageEvent.KEY_UI_DESCRIPTION, content[1]);
        extras.putString(TutorialUtil.StageEvent.KEY_SCENE, SceneTutorial.SCENE);
        extras.putString(TutorialUtil.StageEvent.KEY_ACTION, TutorialSceneActions.ACTION_NAV_ASK_CONFIRM);
        send(extras);
    }
}
