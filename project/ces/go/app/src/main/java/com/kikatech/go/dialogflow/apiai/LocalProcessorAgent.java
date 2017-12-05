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
    public Intent query(String words, Map<String, List<String>> entities, byte queryType) {
        Intent intent = new Intent(Intent.AS_PREV_SCENE, Intent.ACTION_USER_INPUT, words);
        intent.putExtra(Intent.KEY_USER_INPUT, words);
        return intent;
    }

    @Override
    public void resetContexts() {

    }
}
