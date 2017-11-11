package com.kikatech.voice.core.dialogflow.scene;

import android.os.Bundle;

/**
 * Created by bradchang on 2017/11/6.
 */

public abstract class SceneBaseOld {

    final public static String ACTION_UNKNOWN = "input.unknown";

    public final static byte GENERAL_CMD_UNKNOWN = 0x01;

    public interface ISceneCallback {
        void resetContextImpl();

        void onCommand(byte cmd, Bundle parameters);
    }

    final protected ISceneCallback mCallback;

    protected SceneBaseOld(ISceneCallback callback) {
        mCallback = callback;
    }
}
