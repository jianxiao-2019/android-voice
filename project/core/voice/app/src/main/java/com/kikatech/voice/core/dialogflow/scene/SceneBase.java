package com.kikatech.voice.core.dialogflow.scene;

import android.content.Context;

import com.kikatech.voice.core.dialogflow.DialogObserver;
import com.kikatech.voice.core.dialogflow.intent.Intent;

/**
 * Created by tianli on 17-11-10.
 */

public abstract class SceneBase implements DialogObserver {

    protected ISceneFeedback mFeedback;
    protected Context mContext;
    private ISceneManager mSceneManager = null;
    protected SceneStage mStage = idle();

    public SceneBase(Context context, ISceneFeedback feedback) {
        mContext = context.getApplicationContext();
        mFeedback = feedback;
    }

    void attach(ISceneManager manager) {
        mSceneManager = manager;
    }

    void detach() {
        mSceneManager = null;
    }

    void exit() {
        if (mSceneManager != null) {
            mSceneManager.exitScene(this);
        }
    }

    final public Context getContext() {
        return mContext;
    }

    protected abstract String scene();

    protected abstract void onExit();

    protected abstract SceneStage idle();

    @Override
    public void onIntent(Intent intent) {
        if (Intent.ACTION_EXIT.equals(intent.getAction())) {
            SceneStage stage = mStage.next(intent.getAction(), intent.getExtra());
            if (stage != null) {
                mStage = stage;
                stage.action();
            }
        } else {
            onExit();
            mStage = idle();
        }
    }

}
