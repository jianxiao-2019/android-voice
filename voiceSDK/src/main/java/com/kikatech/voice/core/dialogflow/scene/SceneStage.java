package com.kikatech.voice.core.dialogflow.scene;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Pair;

import java.io.Serializable;

/**
 * Created by tianli on 17-11-11.
 */

public abstract class SceneStage implements ISceneStageFeedback, Serializable {

    protected final String TAG = getClass().getSimpleName();
    protected final ISceneFeedback mFeedback;

    protected final SceneBase mSceneBase;

    public SceneStage(@NonNull SceneBase scene, ISceneFeedback feedback) {
        mFeedback = feedback;
        mSceneBase = scene;
    }

    protected void setQueryAnyWords(boolean queryAnyWords) {
        mSceneBase.setQueryWords(queryAnyWords);
    }

    /**
     * Move to next stage according to the action from agent
     *
     * @param action the action from agent
     * @param extra  extra parameters
     */
    public abstract SceneStage next(String action, Bundle extra);

    protected abstract void prepare();

    /**
     * Define the action that the stage should perform, <p>
     * this action will be performed by {@link #doAction doAction} and is synchronized by default. <p>
     * If asynchronized feature is needed(ex: tts), override {@link #doAction doAction} method,
     * and maintain the stage status via {@link ISceneStageFeedback}
     */
    protected abstract void action();

    final protected void exitScene() {
        mSceneBase.exit();
    }

    final protected void exitSceneAndSleep() {
        mSceneBase.sleep();
    }

    final void prepareAction(String scene, String action, SceneStage stage) {
        prepare();
        if (mFeedback != null) {
            mFeedback.onStagePrepared(scene, action, stage);
        }
    }

    /**
     * Perform the action, must be invoked after call {@link #next(String, Bundle) next} <p>
     * Override and call {@link #action()} instead of super if asynchronized feature(ex: tts) is needed, <p>
     * and maintain the stage status via {@link ISceneStageFeedback}
     */
    public void doAction() {
        onStageActionStart();
        action();
        onStageActionDone(false);
    }

    protected void speak(String text) {
        speak(text, null);
    }

    protected void speak(String text, Bundle extras) {
        if (mFeedback != null) {
            mFeedback.onText(text, extras, this);
        }
    }

    protected void speak(Pair<String, Integer>[] pairs, Bundle extras) {
        if (mFeedback != null) {
            mFeedback.onTextPairs(pairs, extras, this);
        }
    }

    protected void send(Bundle extras) {
        if (mFeedback != null) {
            mFeedback.onStageEvent(extras);
        }
    }

    @Override
    public void onStageActionStart() {
    }

    @Override
    public void onStageActionDone(boolean isInterrupted) {
        if (mFeedback != null) {
            mFeedback.onStageActionDone(isInterrupted);
        }
    }

    protected boolean supportEmoji() {
        return false;
    }
}
