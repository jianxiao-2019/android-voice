package com.kikatech.voice.core.dialogflow;

import android.text.TextUtils;

import com.kikatech.voice.core.dialogflow.intent.Intent;
import com.kikatech.voice.core.util.Subscribe;

import java.util.List;

/**
 * Created by tianli on 17-11-12.
 */

public class SceneManager implements DialogObserver {

    private Subscribe<DialogObserver> mSubscribe = new Subscribe<>();

    private String mScene = null;

    private ISceneCallback mCallback;

    public SceneManager(){
    }

    public SceneManager(ISceneCallback callback){
        mCallback = callback;
    }
//    public SceneManager setSceneCallback(ISceneCallback callback) {
//        mCallback = callback;
//        return this;
//    }

    public synchronized void register(String scene, DialogObserver object) {
        mSubscribe.register(scene, object);
    }

    public synchronized void unregister(String scene, DialogObserver object) {
        mSubscribe.unregister(scene, object);
    }

    @Override
    public void onIntent(Intent intent) {
        String scene = intent.getScene();
        if(!TextUtils.isEmpty(scene)){
            if(scene.equals(mScene)){
                notifyObservers(intent);
            }else{
                if(!TextUtils.isEmpty(mScene)) {
                    notifyObservers(new Intent(mScene, Intent.ACTION_EXIT));
                    if(mCallback != null){
                        mCallback.onSceneExit(mScene);
                    }
                }
                if(mCallback != null){
                    mCallback.onSceneEnter(intent.getScene());
                }
                notifyObservers(intent);
            }
        }
    }

    private void notifyObservers(Intent intent){
        String scene = intent.getScene();
        List<DialogObserver> list = mSubscribe.list(scene);
        for(DialogObserver subscriber : list) {
            subscriber.onIntent(intent);
        }
    }

    public interface ISceneCallback{
        void onSceneEnter(String scene);
        void onSceneExit(String scene);
    }
}
