package com.kikatech.voice.core.dialogflow.scene;

import android.os.Bundle;

/**
 * Created by bradchang on 2017/11/6.
 */

public abstract class SceneBase {

    public interface ISceneCallback {
        void resetContextImpl();

        void onCommand(byte cmd, Bundle parameters);
    }

    final ISceneCallback mCallback;

    SceneBase(ISceneCallback callback) {
        mCallback = callback;
    }
}
