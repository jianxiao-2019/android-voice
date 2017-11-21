package com.kikatech.voice.core.dialogflow.intent;

import android.os.Bundle;
import android.text.TextUtils;

/**
 * Created by tianli on 17-11-2.
 */

public class Intent {

    public final static String ACTION_EXIT = "__Exit__";

    public final static String AS_PREV_SCENE = "as_prev_scene";
    public final static String ACTION_ANY_WORDS = "action_any_words";
    public final static String KEY_ANY_WORDS = "any_words";

    private String mScene;
    private String mAction;
    private Bundle mExtra = new Bundle();

    public Intent(String scene) {
        mScene = scene;
    }

    public Intent(String scene, String action) {
        mScene = scene;
        mAction = action;
    }

    public String getAction() {
        return mAction;
    }

    public void setAction(String action) {
        this.mAction = action;
    }

    public void putExtra(String key, String value) {
        mExtra.putString(key, value);
    }

    public Bundle getExtra() {
        return mExtra;
    }

    public String getScene() {
        return mScene;
    }

    public void correctScene(String scene) {
        if(mScene.equals(AS_PREV_SCENE) && !TextUtils.isEmpty(scene)) {
            mScene = scene;
        }
    }
}
