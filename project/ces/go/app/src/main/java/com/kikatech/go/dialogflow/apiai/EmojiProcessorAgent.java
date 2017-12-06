package com.kikatech.go.dialogflow.apiai;

import com.kikatech.voice.core.dialogflow.Agent;
import com.kikatech.voice.core.dialogflow.intent.Intent;
import com.kikatech.voice.service.IDialogFlowService;
import com.kikatech.voice.util.EmojiUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.Map;

/**
 * Created by brad_chang on 2017/12/4.
 */

public class EmojiProcessorAgent extends Agent {
    @Override
    public Intent query(String words, Map<String, List<String>> entities, byte queryType) {
        Intent intent = new Intent(Intent.AS_PREV_SCENE, Intent.ACTION_RCMD_EMOJI, words);
        intent.putExtra(Intent.KEY_RCMD_EMOJI, words);
        return intent;
    }

    @Override
    public void resetContexts() {

    }
}