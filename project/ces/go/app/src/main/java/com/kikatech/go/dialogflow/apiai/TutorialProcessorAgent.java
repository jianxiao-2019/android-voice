package com.kikatech.go.dialogflow.apiai;

import com.kikatech.go.tutorial.dialogflow.SceneTutorial;
import com.kikatech.go.tutorial.dialogflow.TutorialSceneActions;
import com.kikatech.go.util.LogUtil;
import com.kikatech.voice.core.dialogflow.Agent;
import com.kikatech.voice.core.dialogflow.intent.Intent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author SkeeterWang Created on 2018/5/14.
 */

public class TutorialProcessorAgent extends Agent {
    private static final String TAG = "TutorialProcessorAgent";

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
                if (LogUtil.DEBUG) {
                    LogUtil.log(TAG, "Find stop word : " + stop);
                }
                return new Intent(SceneTutorial.SCENE, TutorialSceneActions.ACTION_STOP, stop, nBestWords);
            }
        }
        for (String synonymStop : LocalProcessorCommands.CANCEL_SYNONYM) {
            if (words.contains(synonymStop)) {
                if (LogUtil.DEBUG) {
                    LogUtil.log(TAG, "Find synonym stop word : " + synonymStop);
                }
                return new Intent(SceneTutorial.SCENE, TutorialSceneActions.ACTION_STOP, LocalProcessorCommands.FIXED_CANCEL_SYNONYM, nBestWords);
            }
        }
        return null;
    }

    @Override
    public void resetContexts() {
    }
}
