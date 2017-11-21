package com.kikatech.go.dialogflow.navigation.stage;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.kikatech.go.dialogflow.navigation.NaviSceneUtil;
import com.kikatech.go.util.LogUtil;
import com.kikatech.voice.core.dialogflow.scene.ISceneFeedback;
import com.kikatech.voice.core.dialogflow.scene.SceneBase;
import com.kikatech.voice.core.dialogflow.scene.SceneStage;

/**
 * Created by bradchang on 2017/11/13.
 */

public class StageAskAddress extends BaseNaviStage {

    private final boolean mAgain;

    StageAskAddress(@NonNull SceneBase scene, ISceneFeedback feedback, boolean again) {
        super(scene, feedback);
        mAgain = again;
        if(LogUtil.DEBUG) LogUtil.log(TAG, "StageAskAddress init");
    }

    @Override
    public SceneStage next(String action, Bundle extra) {
        super.next(action, extra);
        if(mStopNavi) {
            return null;
        }

        String naviAddress = NaviSceneUtil.parseAddress(extra);
        if(LogUtil.DEBUG) LogUtil.log(TAG, "naviAddress:" + naviAddress);

        if (TextUtils.isEmpty(naviAddress)) {
            return new StageAskAddress(mSceneBase, mFeedback, true);
        } else {
            return new StageConfirmAddress(mSceneBase, mFeedback, naviAddress);
        }
    }

    @Override
    public void action() {
        String words = mAgain ?
                "Sorry, I couldn't find it. Please say again." : // doc 7
                "Where do you want to go?"; // doc 3
        speak(words);
    }
}