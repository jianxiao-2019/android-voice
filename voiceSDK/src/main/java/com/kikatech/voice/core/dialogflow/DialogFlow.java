package com.kikatech.voice.core.dialogflow;

import android.content.Context;
import android.text.TextUtils;

import com.kikatech.voice.core.dialogflow.intent.Intent;
import com.kikatech.voice.service.dialogflow.IDialogFlowService;
import com.kikatech.voice.service.conf.VoiceConfiguration;
import com.kikatech.voice.util.log.Logger;

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
    private final String[] dbgMsg;

    public DialogFlow(Context context, VoiceConfiguration conf) {
        mAgent = conf.getAgent().create(context.getApplicationContext());
        dbgMsg = new String[]{"", ""};
    }

    public void talk(final String words, final String[] nBestWords, byte queryType, boolean proactive, final IDialogFlowService.IAgentQueryStatus callback) {
        talk(words, nBestWords, null, queryType, proactive, callback);
    }

    public void talk(final String words, final String[] nBestWords, final Map<String, List<String>> entities, final byte queryType, final boolean proactive, final IDialogFlowService.IAgentQueryStatus callback) {
        if (!TextUtils.isEmpty(words)) {
            mExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    if (callback != null) {
                        callback.onStart(proactive);
                    }
                    Intent intent;
                    try {
                        intent = mAgent.query(words, nBestWords, entities, queryType);
                    } catch (Exception e) {
                        if (callback != null) {
                            callback.onError(e);
                        }
                        return;
                    }

                    if (intent == null) {
                        if (callback != null) {
                            callback.onError(new Exception("DialogFlow engine query intent error."));
                        }
                    } else {
                        if (callback != null) {
                            if (Logger.DEBUG) {
                                dbgMsg[0] = intent.getScene() + "-" + intent.getAction();
                                dbgMsg[1] = intent.getBundleDetail();
                            }
                            callback.onComplete(dbgMsg);
                        }
                        onIntent(intent);
                    }
                }
            });
        }
    }

    public void resetContexts() {
        if (Logger.DEBUG) Logger.i("DialogFlow", "resetContexts");
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                mAgent.resetContexts();
            }
        });
    }

    public void setObserver(DialogObserver observer) {
        mObserver = observer;
    }

    private void onIntent(Intent intent) {
//        String scene = intent.getScene();
        if (mObserver != null) {
            mObserver.onIntent(intent);
        }
//        List<DialogObserver> list = mSubscribe.list(scene);
//        for(DialogObserver subscriber : list) {
//            subscriber.onIntent(intent);
//        }
    }
}