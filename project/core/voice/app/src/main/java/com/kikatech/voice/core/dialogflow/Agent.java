package com.kikatech.voice.core.dialogflow;

import com.kikatech.voice.core.dialogflow.intent.Intent;

/**
 * Created by tianli on 17-11-2.
 */

public abstract class Agent {

    public abstract Intent query(String words);

}
