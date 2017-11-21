package com.kikatech.voice.core.dialogflow.scene;

import android.os.Bundle;
import android.support.annotation.NonNull;

/**
 * Created by tianli on 17-11-11.
 */

public abstract class SceneStage {

    public final static String TAG = "SceneStage";
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

    public abstract void prepare();

    /**
     * Perform the action, must be invoked after call {@link #next(String, Bundle) next}
     */
    public abstract void action();

    final protected void exitScene() {
        mSceneBase.exit();
    }

    final void prepareAction(String scene, String action, SceneStage stage) {
        prepare();
        if (mFeedback != null) {
            mFeedback.onStagePrepared(scene, action, stage);
        }
    }

    protected void onActionDone(boolean isEndOfScene) {
        onActionDone(isEndOfScene, false);
    }

    protected void onActionDone(boolean isEndOfScene, boolean isInterrupted) {
        if (mFeedback != null) {
            mFeedback.onStageActionDone(isEndOfScene, isInterrupted);
        }
    }

    protected void speak(String text) {
        speak(text, null, null);
    }

    protected void speak(String text, IDialogFlowFeedback.IToSceneFeedback feedback) {
        speak(text, null, feedback);
    }

    protected void speak(String text, Bundle extras) {
        speak(text, extras, null);
    }

    protected void speak(String text, Bundle extras, IDialogFlowFeedback.IToSceneFeedback feedback) {
        if (mFeedback != null) {
            mDefaultToSceneFeedback.bindFeedback(feedback);
            mFeedback.onText(text, extras, mDefaultToSceneFeedback);
        }
    }

    private IToSceneFeedbackDispatcher mDefaultToSceneFeedback = new IToSceneFeedbackDispatcher();

    private class IToSceneFeedbackDispatcher implements IDialogFlowFeedback.IToSceneFeedback {
        private IDialogFlowFeedback.IToSceneFeedback mToFeedback;

        private void bindFeedback(IDialogFlowFeedback.IToSceneFeedback feedback) {
            mToFeedback = feedback;
        }

        @Override
        public void onTtsStart() {
            if (mToFeedback != null) {
                mToFeedback.onTtsStart();
            }
        }

        @Override
        public void onTtsComplete() {
            onActionDone(isEndOfScene());
            if (mToFeedback != null) {
                mToFeedback.onTtsComplete();
            }
        }

        @Override
        public void onTtsError() {
            onActionDone(isEndOfScene());
            if (mToFeedback != null) {
                mToFeedback.onTtsError();
            }
        }

        @Override
        public void onTtsInterrupted() {
            onActionDone(isEndOfScene(), true);
            if (mToFeedback != null) {
                mToFeedback.onTtsInterrupted();
            }
        }

        @Override
        public boolean isEndOfScene() {
            return mToFeedback != null && mToFeedback.isEndOfScene();
        }
    }
}


