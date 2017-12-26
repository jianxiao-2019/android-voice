package com.kikatech.go.dialogflow.navigation.stage;

import android.content.Context;
import android.os.Bundle;

import com.kikatech.go.dialogflow.AsrConfigUtil;
import com.kikatech.go.dialogflow.SceneUtil;
import com.kikatech.go.dialogflow.model.Option;
import com.kikatech.go.dialogflow.model.OptionList;
import com.kikatech.go.dialogflow.navigation.NaviSceneActions;
import com.kikatech.go.util.LogUtil;
import com.kikatech.voice.core.dialogflow.intent.Intent;
import com.kikatech.voice.core.dialogflow.scene.ISceneFeedback;
import com.kikatech.voice.core.dialogflow.scene.SceneBase;
import com.kikatech.voice.core.dialogflow.scene.SceneStage;

/**
 * Created by tianli on 17-11-11.
 */

public class StageConfirmAddress extends BaseNaviStage {
    private static final String TAG = "StageConfirmAddress";

    private final String mUserInput;
    private final String mNaviAddress;

    StageConfirmAddress(SceneBase scene, ISceneFeedback feedback, String userInput, String naviAddress) {
        super(scene, feedback);
        overrideUncaughtAction = true;
        mUserInput = userInput;
        mNaviAddress = naviAddress;
        if (LogUtil.DEBUG) {
            LogUtil.log(TAG, "StageConfirmAddress init, mNaviAddress:" + mNaviAddress);
        }
    }

    @Override
    @AsrConfigUtil.ASRMode
    protected int getAsrMode() {
        return AsrConfigUtil.ASR_MODE_CONVERSATION_CMD_ALTER;
    }

    @Override
    public SceneStage next(String action, Bundle extra) {
        SceneStage superStage = super.next(action, extra);
        if (superStage != null) {
            return superStage;
        }

        switch (action) {
            case Intent.ACTION_UNCAUGHT:
            case NaviSceneActions.ACTION_NAV_YES:
                return new StageNavigationGo(mSceneBase, mFeedback, mNaviAddress, null, false);
            case NaviSceneActions.ACTION_NAV_NO:
                return new StageAskAddress(mSceneBase, mFeedback, false);
            case NaviSceneActions.ACTION_NAV_CHANGE:
                String userSays = Intent.parseUserInput(extra);
                String[] userInputs = Intent.parseUserInputNBest(extra);
                SceneStage stageGo = getStageByCheckDestination(userInputs);
                return stageGo != null ? stageGo : new StageConfirmAddress(mSceneBase, mFeedback, userSays, userSays);
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
//        supportAsrInterrupted = true;
        Context context = mSceneBase.getContext();
        String[] uiAndTtsText = SceneUtil.getConfirmAddress(context, mUserInput);
        if (uiAndTtsText.length > 0) {
            String[] options = SceneUtil.getOptionsCommon(context);
            String uiText = uiAndTtsText[0];
            String ttsText = uiAndTtsText[1];
            OptionList optionList = new OptionList(OptionList.REQUEST_TYPE_TEXT);
            optionList.setTitle(uiText);
            optionList.setIconRes(SceneUtil.ICON_NAVIGATION);
            for (String option : options) {
                optionList.add(new Option(option, null));
            }
            Bundle args = new Bundle();
            args.putParcelable(SceneUtil.EXTRA_OPTIONS_LIST, optionList);
            speak(ttsText, args);
        }
    }

    @Override
    public Integer overrideAsrBos() {
        return SceneUtil.CONFIRM_BOS_DURATION;
    }
}
