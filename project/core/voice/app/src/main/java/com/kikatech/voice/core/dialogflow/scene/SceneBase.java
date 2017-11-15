package com.kikatech.voice.core.dialogflow.scene;

import android.content.Context;

import com.kikatech.voice.core.dialogflow.DialogObserver;
import com.kikatech.voice.core.dialogflow.intent.Intent;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by tianli on 17-11-10.
 */

public abstract class SceneBase implements DialogObserver {

    protected ISceneFeedback mFeedback;
    protected Context mContext;
    private ISceneManager mSceneManager = null;
    protected SceneStage mStage;

    public SceneBase(Context context, ISceneFeedback feedback) {
        mContext = context.getApplicationContext();
        mFeedback = feedback;
        mStage = idle();
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
        if (!Intent.ACTION_EXIT.equals(intent.getAction())) {
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

    public static class OptionList {
        public static final byte REQUEST_TYPE_ORDINAL = 0x01;
        public static final byte REQUEST_TYPE_TEXT = 0x02;

        private byte requestType;
        private List<Option> options = new ArrayList<>();

        public OptionList(byte requestType) {
            this.requestType = requestType;
        }

        public void add(Option option) {
            this.options.add(option);
        }

        public int size() {
            return options.size();
        }

        public int indexOf(Option option) {
            return options.indexOf(option);
        }

        public boolean isEmpty() {
            return options.isEmpty();
        }

        public Option get(int index) {
            return index > 0 && index < options.size() ? options.get(index) : null;
        }

        public byte getRequestType() {
            return requestType;
        }

        public List<Option> getList() {
            return options;
        }
    }

    public static class Option {
        private String displayText;
        private String nextSceneAction;

        public Option(String displayText, String nextSceneAction) {
            this.displayText = displayText;
            this.nextSceneAction = nextSceneAction;
        }

        public String getDisplayText() {
            return displayText;
        }

        public String getNextSceneAction() {
            return nextSceneAction;
        }
    }
}
