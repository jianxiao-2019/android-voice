package com.kikatech.voice.core.dialogflow.scene;

import com.kikatech.voice.core.dialogflow.DialogObserver;
import com.kikatech.voice.core.dialogflow.intent.Intent;

/**
 * Created by tianli on 17-11-10.
 */

public abstract class SceneBase implements DialogObserver {

    protected SceneStage mStage = init();

    protected abstract SceneStage init();

    @Override
    public void onIntent(Intent intent) {
        SceneStage stage = mStage.next(intent.getAction(), intent.getExtra());
        if (stage != null) {
            mStage = stage;
            stage.action();
        }
    }

}
