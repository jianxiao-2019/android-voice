package com.kikatech.voice.core.dialogflow.scene;

import android.text.TextUtils;

import com.kikatech.voice.core.dialogflow.DialogObserver;
import com.kikatech.voice.core.dialogflow.intent.Intent;
import com.kikatech.voice.core.util.Subscribe;

import java.util.List;

/**
 * Created by tianli on 17-11-12.
 */

public class SceneManager implements DialogObserver, ISceneManager {

    private Subscribe<SceneBase> mSubscribe = new Subscribe<>();

    private String mScene = null;

    private SceneLifecycleObserver mCallback;

    public SceneManager(SceneLifecycleObserver callback) {
        mCallback = callback;
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
        String scene = intent.getScene();
        if (!TextUtils.isEmpty(scene)) {
            if (scene.equals(mScene)) {
                notifyObservers(intent);
            } else {
                doExitScene(mScene);
                mScene = scene;
                if (mCallback != null) {
                    mCallback.onSceneEnter(mScene);
                }
                notifyObservers(intent);
            }
        }
    }

    private void notifyObservers(Intent intent) {
        String scene = intent.getScene();
        List<SceneBase> list = mSubscribe.list(scene);
        for (SceneBase subscriber : list) {
            subscriber.onIntent(intent);
        }
    }

    @Override
    public void exitScene(SceneBase scene) {
        if(!TextUtils.isEmpty(mScene) && mScene.equals(scene.scene())){
            doExitScene(mScene);
            mScene = null;
        }
    }

    private void doExitScene(String scene){
        if (!TextUtils.isEmpty(scene)) {
            notifyObservers(new Intent(scene, Intent.ACTION_EXIT));
            if (mCallback != null) {
                mCallback.onSceneExit(scene);
            }
        }
    }

    public interface SceneLifecycleObserver {
        void onSceneEnter(String scene);

        void onSceneExit(String scene);
    }
}
