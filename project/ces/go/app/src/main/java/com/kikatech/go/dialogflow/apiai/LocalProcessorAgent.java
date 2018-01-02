package com.kikatech.go.dialogflow.apiai;

import com.kikatech.go.dialogflow.stop.SceneActions;
import com.kikatech.go.dialogflow.stop.SceneStopIntent;
import com.kikatech.go.util.LogUtil;
import com.kikatech.voice.core.dialogflow.Agent;
import com.kikatech.voice.core.dialogflow.intent.Intent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by brad_chang on 2017/11/21.
 */

public class LocalProcessorAgent extends Agent {
    private static final String TAG = "LocalProcessorAgent";

    @Override
    public Intent query(String words, String[] nBestWords, Map<String, List<String>> entities, byte queryType) {
        Intent intent = getCancelIntent(nBestWords);
        if (intent == null) {
            intent = new Intent(Intent.AS_PREV_SCENE, Intent.ACTION_USER_INPUT, words, nBestWords);
        }

        if (LogUtil.DEBUG) LogUtil.log(TAG, "Local Intent : " + intent);
        return intent;
    }

    private Intent getCancelIntent(String[] nBestWords) {
        if (nBestWords == null || nBestWords.length == 0) {
            return null;
        }

        List<String> words = new ArrayList<>(nBestWords.length);
        for (String s : nBestWords) {
            words.add(s.toLowerCase().trim());
        }

        for (String stop : LocalProcessorCommands.CANCEL) {
            if (words.contains(stop)) {
                if (LogUtil.DEBUG) LogUtil.log(TAG, "Find stop word : " + stop);
                return new Intent(SceneStopIntent.SCENE, SceneActions.STOP_ACTION, stop, nBestWords);
            }
        }
        return null;
    }

    @Override
    public void resetContexts() {

    }
}