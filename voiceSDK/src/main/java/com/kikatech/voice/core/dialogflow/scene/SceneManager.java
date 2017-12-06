package com.kikatech.voice.core.dialogflow.scene;

import android.text.TextUtils;

import com.kikatech.voice.core.dialogflow.DialogObserver;
import com.kikatech.voice.core.dialogflow.intent.Intent;
import com.kikatech.voice.core.util.Subscribe;
import com.kikatech.voice.util.log.LogUtil;

import java.util.List;

/**
 * Created by tianli on 17-11-12.
 */

public class SceneManager implements DialogObserver, ISceneManager {

    private Subscribe<SceneBase> mSubscribe = new Subscribe<>();

    private String mScene = null;

    private final SceneLifecycleObserver mCallback;
    private final SceneQueryWordsStatus mSceneQueryWordsCallback;
    private boolean mQueryAnyWords = false;

    public SceneManager(SceneLifecycleObserver callback, SceneQueryWordsStatus sqwsCallback) {
        mCallback = callback;
        mSceneQueryWordsCallback = sqwsCallback;
        if (mSceneQueryWordsCallback != null) {
            mSceneQueryWordsCallback.onQueryAnyWordsStatusChange(mQueryAnyWords);
        }
    }

    public void register(SceneBase object) {
        object.attach(this);
        mSubscribe.register(object.scene(), object);
    }

    public void unregister(SceneBase object) {
        object.detach();
        mSubscribe.unregister(object.scene(), object);
    }

    @Override
    public void onIntent(Intent intent) {
        intent.correctScene(mScene);
        String scene = intent.getScene();
        if (LogUtil.DEBUG) {
            LogUtil.log("Intent", String.format("mScene: %s", mScene));
            LogUtil.log("Intent", String.format("scene: %s", scene));
        }
        if (!TextUtils.isEmpty(scene)) {
            if (!TextUtils.isEmpty(mScene)) {
                if (scene.equals(mScene)) {
                    notifyObservers(intent);
                } else {
                    prepareSwitchSceneInfo(scene, intent);
                    doExitScene(mScene, false);
                    mScene = scene;
                    if (mCallback != null) {
                        mCallback.onSceneEnter(mScene);
                    }
                    notifyObservers(intent);
                }
            } else {
                prepareSwitchSceneInfo(scene, intent);
                mScene = scene;
                if (mCallback != null) {
                    mCallback.onSceneEnter(mScene);
                }
                notifyObservers(intent);
            }
        }
    }

    private void prepareSwitchSceneInfo(String targetScene, Intent intent) {
        if (LogUtil.DEBUG) {
            LogUtil.log("Intent", "prepareSwitchSceneInfo, current:"+mScene+", target:" + targetScene+ ", intent:" + intent);
        }
        if (!TextUtils.isEmpty(mScene) && intent != null) {
            SceneBase src = getScene(mScene);
            if (src != null && intent.getExtra() != null) {
                String json = src.getTransformSceneInfo();
                if (json != null) {
                    intent.getExtra().putString(Intent.KEY_SWITCH_SCENE_INFO, json);
                }
                if (LogUtil.DEBUG) {
                    LogUtil.log("Intent", "prepareSwitchSceneInfo, json:" + json);
                }
            }
        }
    }

    private SceneBase getScene(String scene) {
        List<SceneBase> list = mSubscribe.list(scene);
        for (SceneBase subscriber : list) {
            if(subscriber.scene().equals(scene)) {
                return subscriber;
            }
        }
        return null;
    }

    private void notifyObservers(Intent intent) {
        String scene = intent.getScene();
        boolean isEmojiIntent = intent.isEmoji();
        List<SceneBase> list = mSubscribe.list(scene);
        for (SceneBase subscriber : list) {
            if (!isEmojiIntent) {
                subscriber.onIntent(intent);
            } else if (subscriber.supportEmoji()) {
                subscriber.onIntent(intent);
            }
        }
    }

    @Override
    public void exitScene(SceneBase scene) {
        if (!TextUtils.isEmpty(mScene) && mScene.equals(scene.scene())) {
            doExitScene(mScene, true);
            mScene = null;
        }
    }

    @Override
    public void exitCurrentScene() {
        doExitScene(mScene, true);
    }

    @Override
    public void setQueryWords(boolean queryAnyWords) {
        if (mQueryAnyWords != queryAnyWords) {
            mQueryAnyWords = queryAnyWords;
            mSceneQueryWordsCallback.onQueryAnyWordsStatusChange(queryAnyWords);
        }
    }

    private void doExitScene(String scene, boolean proactive) {
        if (!TextUtils.isEmpty(scene)) {
            notifyObservers(new Intent(scene, Intent.ACTION_EXIT));
            if (mCallback != null) {
                mCallback.onSceneExit(scene, proactive);
            }
        }
    }

    public interface SceneLifecycleObserver {
        void onSceneEnter(String scene);

        void onSceneExit(String scene, boolean proactive);
    }

    public interface SceneQueryWordsStatus {
        void onQueryAnyWordsStatusChange(boolean queryAnyWords);
    }
}
