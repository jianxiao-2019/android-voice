package com.kikatech.go.dialogflow.apiai;

import com.kikatech.voice.core.dialogflow.Agent;
import com.kikatech.voice.core.dialogflow.intent.Intent;

import java.util.List;
import java.util.Map;

/**
 * Created by brad_chang on 2017/11/21.
 */

public class LocalProcessorAgent extends Agent {
    @Override
    public Intent query(String words, String[] nBestWords, Map<String, List<String>> entities, byte queryType) {
        return new Intent(Intent.AS_PREV_SCENE, Intent.ACTION_USER_INPUT, words, nBestWords);
    }

    @Override
    public void resetContexts() {

    }
}
