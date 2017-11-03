package com.kikatech.voice.core.dialogflow;

import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.kikatech.voice.VoiceConfiguration;
import com.kikatech.voice.core.dialogflow.intent.Intent;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by tianli on 17-10-28.
 */

public class DialogFlow {

    private Agent mAgent;

    private HashMap<String, List<DialogObserver>> mSubscribers = new HashMap<>();
    private ExecutorService mExecutor = Executors.newSingleThreadExecutor();

    private DialogFlow(Agent agent) {
        mAgent = agent;
    }

    public static DialogFlow getInstance(VoiceConfiguration conf) {
        DialogFlow flow = new DialogFlow(conf.getAgent().create());
        return flow;
    }

    public void talk(final String words) {
        if (!TextUtils.isEmpty(words)) {
            mExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    Intent intent = mAgent.query(words);
                    if(intent != null) {
                        onIntent(intent);
                    }
                }
            });
        }
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

    private void onIntent(Intent intent) {
        String scene = intent.getScene();
        List<DialogObserver> subscribers = new ArrayList<>();
        synchronized (this){
            if(mSubscribers.containsKey(scene)){
                subscribers.addAll(mSubscribers.get(scene));
            }
        }
        for(DialogObserver subscriber : subscribers) {
            subscriber.onIntent(intent);
        }
    }

}
