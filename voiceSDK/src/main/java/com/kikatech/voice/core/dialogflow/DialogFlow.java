package com.kikatech.voice.core.dialogflow;

import android.content.Context;
import android.text.TextUtils;

import com.kikatech.voice.core.dialogflow.intent.Intent;
import com.kikatech.voice.service.IDialogFlowService;
import com.kikatech.voice.service.VoiceConfiguration;
import com.kikatech.voice.util.log.LogUtil;

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
        if(LogUtil.DEBUG) {
            dbgMsg = new String[]{"", ""};
        } else {
            dbgMsg = null;
        }
    }

    public void talk(final String words, byte queryType, final IDialogFlowService.IAgentQueryStatus callback) {
        talk(words, null, queryType, callback);
    }

    public void talk(final String words, final Map<String, List<String>> entities, final byte queryType, final IDialogFlowService.IAgentQueryStatus callback) {
        if (!TextUtils.isEmpty(words)) {
            mExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    if (callback != null) callback.onStart();
                    Intent intent = null;
                    try {
                        intent = mAgent.query(words, entities, queryType);
                    } catch (Exception e) {
                        if (callback != null) callback.onError(e);
                    }

                    if (intent != null) {
                        if (callback != null) {
                            if(LogUtil.DEBUG) {
                                dbgMsg[0] = intent.getScene() + "-" + intent.getAction();
                                dbgMsg[1] = intent.getExtra().toString();
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
        if (LogUtil.DEBUG) LogUtil.log("DialogFlow", "resetContexts");
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