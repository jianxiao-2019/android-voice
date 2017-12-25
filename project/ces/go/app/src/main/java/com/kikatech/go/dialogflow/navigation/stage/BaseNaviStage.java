package com.kikatech.go.dialogflow.navigation.stage;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.kikatech.go.dialogflow.BaseSceneStage;
import com.kikatech.go.dialogflow.UserSettings;
import com.kikatech.go.dialogflow.model.SettingDestination;
import com.kikatech.go.dialogflow.navigation.NaviSceneActions;
import com.kikatech.go.util.LogUtil;
import com.kikatech.go.util.preference.GlobalPref;
import com.kikatech.voice.core.dialogflow.scene.ISceneFeedback;
import com.kikatech.voice.core.dialogflow.scene.SceneBase;
import com.kikatech.voice.core.dialogflow.scene.SceneStage;

import java.util.List;

/**
 * Created by bradchang on 2017/11/14.
 */

public class BaseNaviStage extends BaseSceneStage {

    BaseNaviStage(@NonNull SceneBase scene, ISceneFeedback feedback) {
        super(scene, feedback);
    }

    @Override
    public SceneStage next(String action, Bundle extra) {
        if (LogUtil.DEBUG) {
            LogUtil.log(TAG, "action:" + action);
        }
        if (NaviSceneActions.ACTION_NAV_CANCEL.equals(action)) {
            return new StageCancelNavigation(mSceneBase, mFeedback);
        }
        return null;
    }

    @Override
    public void prepare() {
    }

    @Override
    public void action() {
    }

    SceneStage getStageByCheckDestination(String[] userInputs) {
        if (LogUtil.DEBUG) {
            LogUtil.log(TAG, "getStageByCheckDestination");
        }
        List<SettingDestination> list = UserSettings.getSettingDestinationList();
        List<String> navigatedAddrList = GlobalPref.getIns().getNavigatedAddressList();

        if (list != null && userInputs != null) {
            for (String userInput : userInputs) {
                if (!TextUtils.isEmpty(userInput)) {
                    if (LogUtil.DEBUG) {
                        LogUtil.logv(TAG, String.format("nBest: %1$s", userInput));
                    }
                    String processedUserInput = userInput.trim();
                    processedUserInput = processedUserInput.toLowerCase();
                    for (SettingDestination destination : list) {
                        String name = destination.getName();
                        String address = destination.getAddress();
                        if (LogUtil.DEBUG) {
                            LogUtil.logd(TAG, String.format("name: %1$s, address: %2$s", name, address));
                        }
                        if (processedUserInput.equals(name.toLowerCase()) && !TextUtils.isEmpty(address)) {
                            return new StageNavigationGo(mSceneBase, mFeedback, address);
                        }
                    }
                    for (String address : navigatedAddrList) {
                        if (!TextUtils.isEmpty(address) && processedUserInput.equals(address.toLowerCase())) {
                            if (LogUtil.DEBUG) {
                                LogUtil.logd(TAG, "Skip asking, go to " + address + " directly");
                            }
                            return new StageNavigationGo(mSceneBase, mFeedback, address);
                        }
                    }
                }
            }
        }

        return null;
    }
}