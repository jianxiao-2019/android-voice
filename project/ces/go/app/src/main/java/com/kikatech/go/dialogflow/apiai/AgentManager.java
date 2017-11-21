package com.kikatech.go.dialogflow.apiai;

import android.content.Context;

import com.kikatech.voice.core.dialogflow.Agent;
import com.kikatech.voice.core.dialogflow.intent.Intent;
import com.kikatech.voice.util.log.LogUtil;

import java.util.List;
import java.util.Map;

/**
 * Created by brad_chang on 2017/11/21.
 */

public class AgentManager extends Agent {

    private final ApiAiAgent mApiAiAgent;
    private final NonCommandProcessor mApiAiAssistAgent;

    AgentManager(Context context) {
        mApiAiAgent = new ApiAiAgent(context);
        mApiAiAssistAgent = new NonCommandProcessor();
    }

    @Override
    public Intent query(String words, Map<String, List<String>> entities, boolean anyContent) {
        if(anyContent) {
            if (LogUtil.DEBUG) LogUtil.logd("ApiAiAgent", "query anyContent, words: " + words);
            return mApiAiAssistAgent.query(words, entities, anyContent);
        } else {
            return mApiAiAgent.query(words, entities, anyContent);
        }
    }

    @Override
    public void resetContexts() {
        mApiAiAgent.resetContexts();
    }
}
