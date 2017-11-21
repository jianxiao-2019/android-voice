package com.kikatech.go.dialogflow.apiai;

import com.kikatech.voice.core.dialogflow.Agent;
import com.kikatech.voice.core.dialogflow.intent.Intent;

import java.util.List;
import java.util.Map;

/**
 * Created by brad_chang on 2017/11/21.
 */

public class NonCommandProcessor extends Agent {
    @Override
    public Intent query(String words, Map<String, List<String>> entities, boolean anyContent) {
        Intent intent = new Intent(Intent.AS_PREV_SCENE, Intent.ACTION_ANY_WORDS);
        intent.putExtra(Intent.KEY_ANY_WORDS, words);
        return intent;
    }

    @Override
    public void resetContexts() {

    }
}
