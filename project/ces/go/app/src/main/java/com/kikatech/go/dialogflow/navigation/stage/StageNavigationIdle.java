package com.kikatech.go.dialogflow.navigation.stage;

import android.os.Bundle;
import android.text.TextUtils;

import com.kikatech.go.dialogflow.navigation.NaviSceneActions;
import com.kikatech.go.dialogflow.navigation.NaviSceneUtil;
import com.kikatech.go.dialogflow.stop.stage.StageStopNavigation;
import com.kikatech.go.util.LogUtil;
import com.kikatech.go.util.preference.GlobalPref;
import com.kikatech.voice.core.dialogflow.intent.Intent;
import com.kikatech.voice.core.dialogflow.scene.ISceneFeedback;
import com.kikatech.voice.core.dialogflow.scene.SceneBase;
import com.kikatech.voice.core.dialogflow.scene.SceneStage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by tianli on 17-11-11.
 */

public class StageNavigationIdle extends BaseNaviStage {

    public StageNavigationIdle(SceneBase scene, ISceneFeedback feedback) {
        super(scene, feedback);
    }

    @Override
    public SceneStage next(String action, Bundle extra) {
        super.next(action, extra);

        if (action.equals(NaviSceneActions.ACTION_NAV_START)) {
            String naviAddress = NaviSceneUtil.parseAddress(extra);
            if (LogUtil.DEBUG) LogUtil.log(TAG, "naviAddress:" + naviAddress);

            if (TextUtils.isEmpty(naviAddress)) {
                return new StageAskAddress(mSceneBase, mFeedback, false);
            } else {
                String[] userInputs = Intent.parseUserInputNBest(extra);
                List<String> listToCheck = new ArrayList<>();
                listToCheck.add(naviAddress);
                listToCheck.addAll(Arrays.asList(userInputs));
                SceneStage stageGo = getStageByCheckDestination(listToCheck.toArray(new String[0]));
                return stageGo != null ? stageGo : new StageConfirmAddress(mSceneBase, mFeedback, naviAddress, naviAddress);
            }
        } else if (action.equals(NaviSceneActions.ACTION_NAV_CANCEL)) {
            return new StageStopNavigation(mSceneBase, mFeedback);
        }

        return this;
    }

    @Override
    public void action() {
        if (LogUtil.DEBUG) LogUtil.log(TAG, "action");
    }
}