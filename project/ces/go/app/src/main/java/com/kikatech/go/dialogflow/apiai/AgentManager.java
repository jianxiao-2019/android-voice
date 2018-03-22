package com.kikatech.go.dialogflow.apiai;

import android.content.Context;

import com.kikatech.go.util.LogUtil;
import com.kikatech.voice.core.dialogflow.Agent;
import com.kikatech.voice.core.dialogflow.intent.Intent;
import com.kikatech.voice.service.IDialogFlowService;

import java.util.List;
import java.util.Map;

/**
 * Created by brad_chang on 2017/11/21.
 */

public class AgentManager extends Agent {

    private final ApiAiAgent mApiAiAgent;
    private final LocalProcessorAgent mApiAiAssistAgent;
    private final EmojiProcessorAgent mEmojiProcessorAgent;

    AgentManager(Context context) {
        mApiAiAgent = new ApiAiAgent(context);
        mApiAiAssistAgent = new LocalProcessorAgent();
        mEmojiProcessorAgent = new EmojiProcessorAgent();
    }

    @Override
    public Intent query(String words, String[] nBestWords, Map<String, List<String>> entities, byte queryType) {
        switch (queryType) {
            case IDialogFlowService.QUERY_TYPE_SERVER:
                return mApiAiAgent.query(words, nBestWords, entities, queryType);
            case IDialogFlowService.QUERY_TYPE_LOCAL:
                if (LogUtil.DEBUG) LogUtil.logd("ApiAiAgent", "query anyContent, words: " + words);
                return mApiAiAssistAgent.query(words, nBestWords, entities, queryType);
            case IDialogFlowService.QUERY_TYPE_EMOJI:
                if (LogUtil.DEBUG) LogUtil.logd("ApiAiAgent", "query emoji : " + words);
                return mEmojiProcessorAgent.query(words, nBestWords, entities, queryType);
            default:
                return null;
        }
    }

    @Override
    public void resetContexts() {
        mApiAiAgent.resetContexts();
    }
}
