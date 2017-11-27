package com.kikatech.voice.core.dialogflow.scene;

import android.content.Context;
import android.text.TextUtils;

import com.kikatech.voice.core.dialogflow.DialogObserver;
import com.kikatech.voice.core.dialogflow.intent.Intent;
import com.kikatech.voice.util.log.LogUtil;

/**
 * Created by tianli on 17-11-10.
 */

public abstract class SceneBase implements DialogObserver {

    protected ISceneFeedback mFeedback;
    protected Context mContext;
    private ISceneManager mSceneManager = null;
    private SceneStage mStage;

    public SceneBase(Context context, ISceneFeedback feedback) {
        mContext = context.getApplicationContext();
        mFeedback = feedback;
        mStage = idle();
    }

    void attach(ISceneManager manager) {
        mSceneManager = manager;
    }

    void setQueryWords(boolean queryAnyWords) {
        mSceneManager.setQueryWords(queryAnyWords);
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
        String action = intent.getAction();
        if (TextUtils.isEmpty(action)) {
            return;
        }
        if (LogUtil.DEBUG) {
            LogUtil.logw("SceneBase", "onIntent : " + intent);
        }
        if (Intent.ACTION_EXIT.equals(action)) {
            onExit();
            mStage = idle();
        } else if(Intent.ACTION_UNKNOWN.equals(action)) {
            if (LogUtil.DEBUG) LogUtil.logw("SceneBase", "Unknown Action, repeat current stage");
            if(mStage != null) {
                mStage.prepareAction(scene(), action, mStage);
            }
        } else {
            SceneStage stage = mStage.next(action, intent.getExtra());
            if (stage != null) {
                mStage = stage;
                stage.prepareAction(scene(), action, stage);
            }
        }
    }

    /**
     * for local stages only
     **/
    public void nextStage(SceneStage stage) {
        if (stage != null) {
            mStage = stage;
            stage.prepareAction(scene(), "", stage);
        }
    }
}
