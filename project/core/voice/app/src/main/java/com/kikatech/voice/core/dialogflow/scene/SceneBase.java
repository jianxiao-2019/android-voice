package com.kikatech.voice.core.dialogflow.scene;

import com.kikatech.voice.core.dialogflow.DialogObserver;
import com.kikatech.voice.core.dialogflow.intent.Intent;

/**
 * Created by tianli on 17-11-10.
 */

public abstract class SceneBase implements DialogObserver {

    protected ISceneFeedback mFeedback;

    public SceneBase(ISceneFeedback feedback) {
        mFeedback = feedback;
    }

    protected SceneStage mStage = idle();

    protected abstract void onExit();

    protected abstract SceneStage idle();

    @Override
    public void onIntent(Intent intent) {
        if(Intent.ACTION_EXIT.equals(intent.getAction())){
            SceneStage stage = mStage.next(intent.getAction(), intent.getExtra());
            if (stage != null) {
                mStage = stage;
                stage.action();
            }
        }else{
            onExit();
            mStage = idle();
        }
    }

}
