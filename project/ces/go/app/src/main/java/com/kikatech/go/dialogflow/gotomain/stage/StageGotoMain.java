package com.kikatech.go.dialogflow.gotomain.stage;

import android.os.Bundle;
import android.support.annotation.NonNull;

import com.kikatech.go.dialogflow.BaseSceneStage;
import com.kikatech.go.services.DialogFlowForegroundService;
import com.kikatech.go.util.IntentUtil;
import com.kikatech.go.util.LogUtil;
import com.kikatech.voice.core.dialogflow.scene.ISceneFeedback;
import com.kikatech.voice.core.dialogflow.scene.SceneBase;
import com.kikatech.voice.core.dialogflow.scene.SceneStage;

/**
 * Created by brad_chang on 2017/12/28.
 */

public class StageGotoMain extends BaseSceneStage {
    public StageGotoMain(@NonNull SceneBase scene, ISceneFeedback feedback) {
        super(scene, feedback);
    }

    @Override
    public SceneStage next(String action, Bundle extra) {
        return null;
    }

    @Override
    protected void prepare() {

    }

    @Override
    protected void action() {
        boolean isAppForeground = DialogFlowForegroundService.isAppForeground();
        if (LogUtil.DEBUG) {
            LogUtil.log(TAG, "StageGotoMain !!! DialogFlowForegroundService isAppForeground:" + isAppForeground);
        }

        exitScene();

        if (!isAppForeground) {
            IntentUtil.openKikaGo(mSceneBase.getContext());
        }
    }
}