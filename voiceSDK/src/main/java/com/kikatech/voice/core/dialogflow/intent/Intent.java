package com.kikatech.voice.core.dialogflow.intent;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.kikatech.voice.util.log.LogUtil;

/**
 * Created by tianli on 17-11-2.
 */

public class Intent {

    public final static String ACTION_EXIT = "__Exit__";
    public final static String ACTION_UNKNOWN = "input.unknown";

    public final static String DEFAULT_SCENE = "Default";

    // Process user custom query
    public final static String AS_PREV_SCENE = "as_prev_scene";
    public final static String ACTION_USER_INPUT = "custom_intent_action_user_input";
    public final static String KEY_USER_INPUT = "custom_intent_key_user_input";

    // Process Emoji
    public final static String ACTION_RCMD_EMOJI = "custom_intent_action_rcmd_emoji";
    public final static String KEY_RCMD_EMOJI = "custom_intent_key_rcmd_emoji";

    public final static String KEY_SWITCH_SCENE_INFO = "switch_scene_info";

    private String mScene;
    private String mAction;
    private final Bundle mExtra = new Bundle();

    public Intent(String scene, String action) {
        mScene = scene;
        mAction = action;
    }

    public Intent(String scene, String action, String resolvedQuery) {
        mScene = scene;
        mAction = action;
        mExtra.putString(KEY_USER_INPUT, resolvedQuery);
    }

    @Override
    public String toString() {
        return mScene + " - " + mAction + " : " + mExtra;
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
        if (LogUtil.DEBUG)
            LogUtil.log("Intent", "scene:" + mScene + ", mAction:" + mAction);
        if (!TextUtils.isEmpty(scene) && (mScene.equals(AS_PREV_SCENE))) {
            if (LogUtil.DEBUG)
                LogUtil.log("Intent", "Find " + mScene + ", correct scene to " + scene);
            mScene = scene;
        } else if (
                mScene.equals(Intent.DEFAULT_SCENE) && !TextUtils.isEmpty(scene) &&
                        !mScene.equals(scene) && !TextUtils.isEmpty(mAction) && mAction.equals(ACTION_UNKNOWN)) {
            mScene = scene;
            if (LogUtil.DEBUG)
                LogUtil.log("Intent", "Find Default::input.unknown, let the current scene " + mScene + " to handle it");
        }
    }

    public static String parseUserInput(@NonNull Bundle extra) {
        return extra.getString(KEY_USER_INPUT, "");
    }

    public static String parseSwitchSceneInfo(@NonNull Bundle extra) {
        return extra.getString(KEY_SWITCH_SCENE_INFO, "");
    }

    public static String parseEmojiJsonString(@NonNull Bundle extra) {
        return extra.getString(KEY_RCMD_EMOJI, "");
    }

    public boolean isEmoji() {
        return mAction.equals(ACTION_RCMD_EMOJI);
    }
}