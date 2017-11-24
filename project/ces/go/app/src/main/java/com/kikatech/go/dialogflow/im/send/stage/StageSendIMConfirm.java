package com.kikatech.go.dialogflow.im.send.stage;

import android.support.annotation.NonNull;

import com.kikatech.go.util.LogUtil;
import com.kikatech.voice.core.dialogflow.scene.ISceneFeedback;
import com.kikatech.voice.core.dialogflow.scene.SceneBase;

/**
 * Created by brad_chang on 2017/11/24.
 */

public class StageSendIMConfirm extends BaseSendIMStage {
    StageSendIMConfirm(@NonNull SceneBase scene, ISceneFeedback feedback) {
        super(scene, feedback);
    }

    @Override
    public void action() {
        if(LogUtil.DEBUG) LogUtil.log(TAG, "Send IM !!!!" + getIMContent().toString());
    }
}
