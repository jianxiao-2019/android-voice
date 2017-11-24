package com.kikatech.go.dialogflow.im.send.stage;

import android.os.Bundle;
import android.support.annotation.NonNull;

import com.kikatech.go.util.LogUtil;
import com.kikatech.voice.core.dialogflow.scene.ISceneFeedback;
import com.kikatech.voice.core.dialogflow.scene.SceneBase;
import com.kikatech.voice.core.dialogflow.scene.SceneStage;

/**
 * Created by brad_chang on 2017/11/23.
 */

public class StageAskIMApp extends BaseSendIMStage {

    private final boolean mIMNotSupported;
    /**
     * 6.1 詢問使用何者 IM
     */
    StageAskIMApp(@NonNull SceneBase scene, ISceneFeedback feedback, boolean IMNotSupported) {
        super(scene, feedback);
        mIMNotSupported = IMNotSupported;
    }

    @Override
    protected SceneStage getNextStage(String action, Bundle extra) {
        return getCheckIMAppStage(TAG, getIMContent(), mSceneBase, mFeedback);
    }

    @Override
    public void action() {
        if (LogUtil.DEBUG) LogUtil.log(TAG, "mIMNotSupported:" + mIMNotSupported);
        if(mIMNotSupported) {
            speak("The app you said is not supported, Which messaging app do you want to use ?");
        } else {
            speak("Which messaging app do you want to use ?");
        }
    }
}
