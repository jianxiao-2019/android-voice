package com.kikatech.voice.core.dialogflow;

import com.kikatech.voice.core.dialogflow.intent.Intent;

import java.util.List;
import java.util.Map;

/**
 * Created by tianli on 17-11-2.
 */

public abstract class Agent {

    public abstract Intent query(final String words, final String[] nBestWords, final Map<String, List<String>> entities, final byte queryType);

    public abstract void resetContexts();

}
