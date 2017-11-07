package com.kikatech.voice.core.dialogflow;

import android.content.Context;
import android.text.TextUtils;

import com.kikatech.voice.service.VoiceConfiguration;
import com.kikatech.voice.core.dialogflow.intent.Intent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by tianli on 17-10-28.
 */

public class DialogFlow {

    private Agent mAgent;

    private HashMap<String, List<DialogObserver>> mSubscribers = new HashMap<>();
    private ExecutorService mExecutor = Executors.newSingleThreadExecutor();
    private final List<DialogObserver> mListeningSubscribers;

    public DialogFlow(Context context, VoiceConfiguration conf) {
        mAgent = conf.getAgent().create(context.getApplicationContext());
        mListeningSubscribers = new ArrayList<>();
    }

    public void talk(final String words) {
        talk(words, null);
    }

    public void talk(final String words, final Map<String, List<String>> entities) {
        if (!TextUtils.isEmpty(words)) {
            mExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    Intent intent = mAgent.query(words, entities);
                    if (intent != null) {
                        onIntent(intent);
                    }
                }
            });
        }
    }

    public void resetContexts() {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                mAgent.resetContexts();
            }
        });
    }

    public synchronized void register(String scene, DialogObserver observer) {
        if (!TextUtils.isEmpty(scene) && observer != null) {
            if (mSubscribers.get(scene) == null) {
                List<DialogObserver> objectList = new ArrayList<>();
                objectList.add(observer);
                mSubscribers.put(scene, objectList);
            } else {
                if (!mSubscribers.get(scene).contains(observer)) {
                    mSubscribers.get(scene).add(observer);
                }
            }
        }
    }

    public synchronized void unregister(String scene, DialogObserver object) {
        if (!TextUtils.isEmpty(scene) && object != null) {
            List<DialogObserver> list = mSubscribers.get(scene);
            if (list != null) {
                list.remove(object);
                if(list.isEmpty()){
                    mSubscribers.remove(scene);
                }
            }
        }
    }

    public List<DialogObserver> getListeningSubscribers() {
        return mListeningSubscribers;
    }

    private void onIntent(Intent intent) {
        String scene = intent.getScene();
        mListeningSubscribers.clear();
        synchronized (this){
            if(mSubscribers.containsKey(scene)){
                mListeningSubscribers.addAll(mSubscribers.get(scene));
            }
        }
        for(DialogObserver subscriber : mListeningSubscribers) {
            subscriber.onIntent(intent);
        }
    }
}