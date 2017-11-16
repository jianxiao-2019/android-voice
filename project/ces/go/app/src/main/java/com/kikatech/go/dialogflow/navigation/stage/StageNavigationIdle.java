package com.kikatech.go.dialogflow.navigation.stage;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.kikatech.go.dialogflow.navigation.NaviSceneActions;
import com.kikatech.go.dialogflow.navigation.NaviSceneUtil;
import com.kikatech.go.dialogflow.navigation.SceneNavigation;
import com.kikatech.go.util.LogUtil;
import com.kikatech.voice.core.dialogflow.scene.ISceneFeedback;
import com.kikatech.voice.core.dialogflow.scene.SceneBase;
import com.kikatech.voice.core.dialogflow.scene.SceneStage;

import org.json.JSONException;
import org.json.JSONObject;

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

        if(action.equals(NaviSceneActions.ACTION_NAV_START)) {
            String naviAddress = NaviSceneUtil.parseAddress(extra);
            if(LogUtil.DEBUG) LogUtil.log(TAG, "naviAddress:" + naviAddress);

            if (TextUtils.isEmpty(naviAddress)) {
                return new StageAskAddress(mSceneBase, mFeedback, false);
            } else {
                return new StageConfirmAddress(mSceneBase, mFeedback, naviAddress);
            }
        }

        return null;
    }

    @Override
    public void action() {
        if(LogUtil.DEBUG) LogUtil.log(TAG, "action");
    }
}