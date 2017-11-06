package com.kikatech.voice.core.dialogflow.intent;

import android.os.Bundle;

/**
 * Created by tianli on 17-11-2.
 */

public class Intent {

    private String mScene;
    private String mName;
    private String mAction;
    private Bundle mExtra = new Bundle();

    public Intent(String scene, String name) {
        mScene = scene;
        mName = name;
    }

    public Intent(String scene, String name, String action) {
        mScene = scene;
        mName = name;
        mAction = action;
    }

    public String getAction() {
        return mAction;
    }

    public void setAction(String action) {
        this.mAction = action;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        this.mName = name;
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

}
