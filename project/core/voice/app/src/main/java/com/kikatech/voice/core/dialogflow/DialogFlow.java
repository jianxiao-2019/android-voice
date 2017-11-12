package com.kikatech.voice.core.dialogflow;

import android.content.Context;
import android.text.TextUtils;

import com.kikatech.voice.core.dialogflow.intent.Intent;
import com.kikatech.voice.service.VoiceConfiguration;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by tianli on 17-10-28.
 */

public class DialogFlow {

    private Agent mAgent;

    private DialogObserver mObserver;
    private ExecutorService mExecutor = Executors.newSingleThreadExecutor();

    public DialogFlow(Context context, VoiceConfiguration conf) {
        mAgent = conf.getAgent().create(context.getApplicationContext());
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

    public void setObserver(DialogObserver observer){
        mObserver = observer;
    }

    private void onIntent(Intent intent) {
//        String scene = intent.getScene();
        if(mObserver != null){
            mObserver.onIntent(intent);
        }
//        List<DialogObserver> list = mSubscribe.list(scene);
//        for(DialogObserver subscriber : list) {
//            subscriber.onIntent(intent);
//        }
    }
}