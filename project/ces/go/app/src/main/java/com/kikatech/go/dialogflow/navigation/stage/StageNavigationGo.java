package com.kikatech.go.dialogflow.navigation.stage;

import android.os.Bundle;

import com.kikatech.go.dialogflow.SceneUtil;
import com.kikatech.go.dialogflow.navigation.NaviSceneUtil;
import com.kikatech.go.util.LogUtil;
import com.kikatech.voice.core.dialogflow.scene.IDialogFlowFeedback;
import com.kikatech.voice.core.dialogflow.scene.ISceneFeedback;
import com.kikatech.voice.core.dialogflow.scene.SceneBase;
import com.kikatech.voice.core.dialogflow.scene.SceneStage;

/**
 * Created by tianli on 17-11-11.
 */

public class StageNavigationGo extends BaseNaviStage {

    private final String mNaviAddress;
    StageNavigationGo(SceneBase scene, ISceneFeedback feedback, String naviAddress) {
        super(scene, feedback);
        mNaviAddress = naviAddress;
        if(LogUtil.DEBUG) LogUtil.log(TAG, "StageNavigationGo init, mNaviAddress:" + mNaviAddress);
    }

    @Override
    public SceneStage next(String action, Bundle extra) {
        return null;
    }

    @Override
    public void action() {
        String[] uiAndTtsText = SceneUtil.getStartNavigation(mSceneBase.getContext());
        if (uiAndTtsText.length > 0) {
            String uiText = uiAndTtsText[0];
            String ttsText = uiAndTtsText[1];
            Bundle args = new Bundle();
            args.putString(SceneUtil.EXTRA_UI_TEXT, uiText);
            speak(ttsText, args, new IDialogFlowFeedback.IToSceneFeedback() {
                @Override
                public void onTtsStart() {
                }

                @Override
                public void onTtsComplete() {
                    startNavigate();
                }

                @Override
                public void onTtsError() {
                    startNavigate();
                }

                @Override
                public void onTtsInterrupted() {
                    startNavigate();
                }

                @Override
                public boolean isEndOfScene() {
                    return true;
                }
            });
        }
    }

    private void startNavigate() {
        NaviSceneUtil.navigateToLocation(mSceneBase.getContext(), mNaviAddress);
        exitScene();
    }
}
