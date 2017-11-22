package com.kikatech.go.dialogflow.stop.stage;

import android.os.Bundle;
import android.support.annotation.NonNull;

import com.kikatech.go.dialogflow.SceneUtil;
import com.kikatech.go.dialogflow.navigation.NaviSceneUtil;
import com.kikatech.go.dialogflow.stop.SceneStopIntent;
import com.kikatech.voice.core.dialogflow.scene.IDialogFlowFeedback;
import com.kikatech.voice.core.dialogflow.scene.ISceneFeedback;
import com.kikatech.voice.core.dialogflow.scene.SceneBase;
import com.kikatech.voice.core.dialogflow.scene.SceneStage;

/**
 * Created by brad_chang on 2017/11/21.
 */

public class StageStopNavigation extends SceneStage {
    StageStopNavigation(@NonNull SceneBase scene, ISceneFeedback feedback) {
        super(scene, feedback);
    }

    @Override
    public SceneStage next(String action, Bundle extra) {
        return null;
    }

    @Override
    public void prepare() {

    }

    @Override
    public void action() {
        String ttsText = SceneUtil.getStopNavigation(mSceneBase.getContext());
        speak(ttsText, new IDialogFlowFeedback.IToSceneFeedback() {
            @Override
            public void onTtsStart() {
            }

            @Override
            public void onTtsComplete() {
                stopNavigation();
            }

            @Override
            public void onTtsError() {
                stopNavigation();
            }

            @Override
            public void onTtsInterrupted() {
                stopNavigation();
            }

            @Override
            public boolean isEndOfScene() {
                return true;
            }
        });
    }

    private void stopNavigation() {
        NaviSceneUtil.stopNavigation(mSceneBase.getContext(), ((SceneStopIntent)mSceneBase).getMainUIClass());
        exitScene();
    }
}
