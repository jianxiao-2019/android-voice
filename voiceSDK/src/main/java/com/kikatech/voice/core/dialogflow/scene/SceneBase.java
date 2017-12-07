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
    private static final String TAG = "SceneBase";

    protected ISceneFeedback mFeedback;
    protected Context mContext;
    private ISceneManager mSceneManager = null;
    private SceneStage mStage;

    private final static int BACK_TO_MAIN_ERR_COUNT = 2;

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

    void sleep() {
        if (mSceneManager != null) {
            mSceneManager.exitSceneAndSleep(this);
        }
    }

    final public Context getContext() {
        return mContext;
    }

    protected abstract String scene();

    protected abstract void onExit();

    protected abstract SceneStage idle();

    protected abstract SceneStage onOverCounts();

    protected String getTransformSceneInfo() {
        return null;
    }

    boolean supportEmoji() {
        return mStage.supportEmoji();
    }

    private int mStageCondCount = 1;

    @Override
    public void onIntent(Intent intent) {
        String action = intent.getAction();
        if (TextUtils.isEmpty(action)) {
            return;
        }
        if (LogUtil.DEBUG) {
            LogUtil.logw(TAG, "onIntent : " + intent);
        }
        if (Intent.ACTION_EXIT.equals(action)) {
            onExit();
            mStage = idle();
        } else {
            boolean isDefaultScene = Intent.DEFAULT_SCENE.equals(scene());
            boolean isUnknownIntent = Intent.ACTION_UNKNOWN.equals(action);
            boolean isUserInput = Intent.ACTION_USER_INPUT.equals(action);
            SceneStage stage = mStage.next(action, intent.getExtra());
            if (stage != null) {
                if (isUserInput || isUnknownIntent) {
                    boolean enterAgain = mStage.getClass().equals(stage.getClass());
                    if (enterAgain) {
                        mStageCondCount += 1;
                        if (LogUtil.DEBUG) {
                            LogUtil.log(TAG, String.format("<%1$s> EnterCount: %2$s", mStage.getClass(), mStageCondCount));
                        }
                    } else {
                        mStageCondCount = 1;
                    }
                    if (mStageCondCount > BACK_TO_MAIN_ERR_COUNT) {
                        if (LogUtil.DEBUG) {
                            LogUtil.logw(TAG, String.format("Enter Count = %s, Go back to main page", mStageCondCount));
                        }
                        mStage = onOverCounts();
                        mStageCondCount = 1;
                    } else if ((isDefaultScene && isUnknownIntent) || !isUnknownIntent) {
                        mStage = stage;
                    }
                    mStage.prepareAction(scene(), action, mStage);
                } else {
                    mStage = stage;
                    mStage.prepareAction(scene(), action, mStage);
                }
            } else if (!isDefaultScene && isUnknownIntent) {
                mStage.prepareAction(scene(), action, mStage);
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