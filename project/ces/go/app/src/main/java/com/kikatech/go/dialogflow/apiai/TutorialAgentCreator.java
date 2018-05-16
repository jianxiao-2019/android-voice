package com.kikatech.go.dialogflow.apiai;

import android.content.Context;

import com.kikatech.voice.core.dialogflow.Agent;
import com.kikatech.voice.core.dialogflow.AgentCreator;

/**
 * @author SkeeterWang Created on 2018/5/14.
 */

public class TutorialAgentCreator extends AgentCreator {
    @Override
    public Agent create(Context context) {
        return new TutorialProcessorAgent();
    }
}
