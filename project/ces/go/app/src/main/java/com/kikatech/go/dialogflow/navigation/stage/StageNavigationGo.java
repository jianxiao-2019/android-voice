package com.kikatech.go.dialogflow.navigation.stage;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;

import com.kikatech.go.dialogflow.SceneUtil;
import com.kikatech.go.dialogflow.model.TtsText;
import com.kikatech.go.dialogflow.navigation.NaviSceneUtil;
import com.kikatech.go.util.TipsHelper;
import com.kikatech.go.util.LogUtil;
import com.kikatech.go.util.preference.GlobalPref;
import com.kikatech.voice.core.dialogflow.scene.ISceneFeedback;
import com.kikatech.voice.core.dialogflow.scene.SceneBase;
import com.kikatech.voice.core.dialogflow.scene.SceneStage;

/**
 * Created by tianli on 17-11-11.
 */

public class StageNavigationGo extends BaseNaviStage {

    private final String mNaviPlaceName;
    private final String mNaviAddress;
    private final boolean mSpeakDestination;

    StageNavigationGo(SceneBase scene, ISceneFeedback feedback, String naviAddress, String naviPlaceName, boolean speakDestination) {
        super(scene, feedback);
        mNaviAddress = naviAddress;
        mNaviPlaceName = naviPlaceName;
        mSpeakDestination = speakDestination;
        GlobalPref.getIns().addNavigatedAddress(mNaviAddress);
        if (LogUtil.DEBUG) {
            LogUtil.log(TAG, "StageNavigationGo init, mNaviAddress:" + mNaviAddress);
        }
    }

    @Override
    public SceneStage next(String action, Bundle extra) {
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
        String[] uiAndTtsText;
        if (mSpeakDestination) {
            String destinationToSpeak = !TextUtils.isEmpty(mNaviPlaceName) ? mNaviPlaceName : mNaviAddress;
            uiAndTtsText = SceneUtil.getStartNavigationWithDestination(context, destinationToSpeak);
        } else {
            uiAndTtsText = SceneUtil.getStartNavigation(context);
        }
        String extraTtsTip = GlobalPref.getIns().getKeepShowingFloatingUiTip() ? SceneUtil.getFirstSleepTip(context) : "";
        if (uiAndTtsText.length > 0) {
            String uiText = uiAndTtsText[0];
            String ttsText = uiAndTtsText[1] + extraTtsTip;
            TtsText tText = new TtsText(SceneUtil.ICON_NAVIGATION, uiText);
            Bundle args = new Bundle();
            args.putParcelable(SceneUtil.EXTRA_TTS_TEXT, tText);
            speak(ttsText, args);
        }
        NaviSceneUtil.navigateToLocation(mSceneBase.getContext(), mNaviAddress);
    }

    @Override
    public void onStageActionDone(boolean isInterrupted) {
        if (LogUtil.DEBUG) {
            LogUtil.log(TAG, "onStageActionDone, isInterrupted:" + isInterrupted);
        }
        TipsHelper.setCanShowDialogMoreCommands(true);
        exitScene();
    }
}
