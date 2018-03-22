package com.kikatech.voice.core.dialogflow.scene;

import android.content.Context;
import android.text.TextUtils;

import com.kikatech.voice.core.dialogflow.DialogObserver;
import com.kikatech.voice.core.dialogflow.intent.Intent;
import com.kikatech.voice.service.conf.AsrConfiguration;
import com.kikatech.voice.util.log.Logger;

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

    AsrConfiguration getAsrConfig() {
        return mStage.getAsrConfig();
    }

    private int mStageCondCount = 1;

    @Override
    public synchronized void onIntent(Intent intent) {
        String action = intent.getAction();
        if (TextUtils.isEmpty(action)) {
            return;
        }
        if (Logger.DEBUG) {
            Logger.w(TAG, "Current:" + this);
            Logger.w(TAG, "onIntent : [" + intent.getScene() + "-" + intent.getAction() + "]" + intent.getBundleDetail());
        }
        if (Intent.ACTION_EXIT.equals(action)) {
            onExit();
            mStage = idle();
            if (Logger.DEBUG) Logger.w(TAG, "set mStage:" + mStage);
        } else {
            boolean isDefaultScene = Intent.DEFAULT_SCENE.equals(scene());
            boolean isUnknownIntent = Intent.ACTION_UNKNOWN.equals(action);
            boolean isUncaughtIntent = Intent.ACTION_UNCAUGHT.equals(action);
            boolean isUserInput = Intent.ACTION_USER_INPUT.equals(action);

            boolean isDefaultUnknown = isDefaultScene && isUnknownIntent;
            boolean isDefaultUncaught = isDefaultScene && isUncaughtIntent;

            boolean isOverrideUncaughtAction = mStage != null && mStage.overrideUncaughtAction;
            boolean isOverrideUnknownAction = mStage != null && mStage.overrideUnknownAction;

            boolean toStayCurrentStage = !isDefaultUnknown && !isDefaultUncaught && !isOverrideUncaughtAction && (isUnknownIntent || isUncaughtIntent);
            boolean toCheckStayCount = isUserInput || isUnknownIntent || isUncaughtIntent;

            SceneStage nextStage;
            if (toStayCurrentStage && !isOverrideUncaughtAction && !isOverrideUnknownAction) {
                nextStage = mStage;
            } else {
                nextStage = mStage.next(action, intent.getExtra());
            }

            if (Logger.DEBUG) {
                Logger.w(TAG, "mStage:" + mStage + ", toStayCurrentStage:" + toStayCurrentStage);
                Logger.w(TAG, "nextStage:" + nextStage);
                Logger.w(TAG, "isDefaultUncaught:" + isDefaultUncaught);
            }

            if (nextStage == null) {
                Logger.w(TAG, "[Warning] nextStage is null !!");
                return;
            }

            if (toCheckStayCount) {
                if (isOverCounts(nextStage)) {
                    nextStage = onOverCounts();
                    mStageCondCount = 1;
                }
            } else {
                mStageCondCount = 1;
            }

            mStage = nextStage;
            if(Logger.DEBUG) Logger.w(TAG, "set mStage:" + mStage);
            mStage.isUncaughtLoop = isUncaughtIntent && !isOverrideUncaughtAction;
            mStage.isDefaultUncaught = isDefaultUncaught;
            mStage.prepareAction(scene(), action, mStage);
        }
    }

    private boolean isOverCounts(SceneStage nextStage) {
        boolean enterAgain = mStage.getClass().equals(nextStage.getClass());
        mStageCondCount = enterAgain ? mStageCondCount + 1 : 1;
        if (Logger.DEBUG) {
            Logger.v(TAG, String.format("mStage: %1$s, nextStage: %2$s, EnterCount: %3$s", mStage.getClass().getSimpleName(), nextStage.getClass().getSimpleName(), mStageCondCount));
        }
        return mStageCondCount > BACK_TO_MAIN_ERR_COUNT;
    }

    /**
     * for local stages only
     **/
    public void nextStage(SceneStage stage) {
        if (stage != null) {
            mStage = stage;
            if(Logger.DEBUG) Logger.w(TAG, "set mStage:" + mStage);
            stage.prepareAction(scene(), "", stage);
        }
    }

    /**
     * Perform cross interactions between different Scene
     * @param intent re-wrapped Intent to a different Scene
     */
    public void redirectIntent(final Intent intent) {
        if (mSceneManager != null) {
            mSceneManager.redirectIntent(intent);
        }
    }
}